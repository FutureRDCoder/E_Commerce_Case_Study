package com.ecommerce.service;

import com.ecommerce.dto.AuthResponse;
import com.ecommerce.dto.LoginRequest;
import com.ecommerce.dto.RegisterRequest;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Role;
import com.ecommerce.model.Tenant;
import com.ecommerce.model.User;
import com.ecommerce.repository.TenantRepository;
import com.ecommerce.repository.UserRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final KeycloakAdminService keycloakAdminService;
    private final KeycloakTokenService keycloakTokenService;
    private final JwtDecoder jwtDecoder;
    private final UserIdentityService userIdentityService;

    public AuthService(
            UserRepository userRepository,
            TenantRepository tenantRepository,
            KeycloakAdminService keycloakAdminService,
            KeycloakTokenService keycloakTokenService,
            JwtDecoder jwtDecoder,
            UserIdentityService userIdentityService) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.keycloakAdminService = keycloakAdminService;
        this.keycloakTokenService = keycloakTokenService;
        this.jwtDecoder = jwtDecoder;
        this.userIdentityService = userIdentityService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already in use");
        }

        Role role = request.getRole();
        if (role == null) {
            role = (request.getTenantSlug() != null && !request.getTenantSlug().isBlank()) ? Role.TENANT_ADMIN : Role.USER;
        }

        Tenant tenant = null;
        if (request.getTenantSlug() != null && !request.getTenantSlug().isBlank()) {
            tenant = tenantRepository.findBySlugIgnoreCase(request.getTenantSlug())
                    .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with slug: " + request.getTenantSlug()));
        }

        String keycloakUserId = keycloakAdminService.createUser(request, role);
        User user = User.builder()
                .name(request.getName())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(null)
                .keycloakUserId(keycloakUserId)
                .role(role)
                .tenant(tenant)
                .build();

        User savedUser = userRepository.save(user);
        String token = keycloakTokenService.loginAndGetAccessToken(request.getUsername(), request.getPassword());

        return AuthResponse.builder()
                .token(token)
                .userId(savedUser.getId())
                .name(savedUser.getName())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .tenantId(savedUser.getTenant() != null ? savedUser.getTenant().getId() : null)
                .tenantSlug(savedUser.getTenant() != null ? savedUser.getTenant().getSlug() : null)
                .tenantName(savedUser.getTenant() != null ? savedUser.getTenant().getName() : null)
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        String token = keycloakTokenService.loginAndGetAccessToken(request.getUsername(), request.getPassword());
        Jwt jwt = decodeJwt(token);
        User user = userIdentityService.resolveOrProvisionUserFromJwt(jwt);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .tenantId(user.getTenant() != null ? user.getTenant().getId() : null)
                .tenantSlug(user.getTenant() != null ? user.getTenant().getSlug() : null)
                .tenantName(user.getTenant() != null ? user.getTenant().getName() : null)
                .build();
    }

    private Jwt decodeJwt(String token) {
        try {
            return jwtDecoder.decode(token);
        } catch (Exception e) {
            return parseJwtUnverified(token);
        }
    }

    private Jwt parseJwtUnverified(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length >= 2) {
                String payloadJson = new String(java.util.Base64.getUrlDecoder().decode(parts[1]), java.nio.charset.StandardCharsets.UTF_8);
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.Map<String, Object> claims = mapper.readValue(payloadJson, new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {});
                
                java.time.Instant iat = java.time.Instant.now();
                java.time.Instant exp = iat.plusSeconds(3600);
                if (claims.get("iat") instanceof Number n) iat = java.time.Instant.ofEpochSecond(n.longValue());
                if (claims.get("exp") instanceof Number n) exp = java.time.Instant.ofEpochSecond(n.longValue());

                return new Jwt(token, iat, exp, java.util.Map.of("alg", "none"), claims);
            }
        } catch (Exception ignored) {}
        throw new BadRequestException("Unable to decode authentication token");
    }
}
