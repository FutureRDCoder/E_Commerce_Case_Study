package com.ecommerce.service;

import com.ecommerce.dto.response.AuthResponse;
import com.ecommerce.dto.request.LoginRequest;
import com.ecommerce.dto.request.RegisterRequest;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Role;
import com.ecommerce.model.Tenant;
import com.ecommerce.model.User;
import com.ecommerce.repository.TenantRepository;
import com.ecommerce.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
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

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        log.info("Registration request received for username='{}', email='{}'",
                request.getUsername(), request.getEmail());

        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed: username '{}' already exists.", request.getUsername());
            throw new BadRequestException("Username is already taken.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: email '{}' already exists.", request.getEmail());
            throw new BadRequestException("Email is already in use.");
        }

        Role role = (request.getTenantSlug() != null && !request.getTenantSlug().isBlank())
                ? Role.TENANT_ADMIN
                : Role.USER;

        Tenant tenant = null;

        if (request.getTenantSlug() != null && !request.getTenantSlug().isBlank()) {

            log.debug("Resolving tenant '{}'", request.getTenantSlug());

            tenant = tenantRepository.findBySlugIgnoreCase(request.getTenantSlug())
                    .orElseThrow(() -> {
                        log.warn("Registration failed: tenant '{}' not found.", request.getTenantSlug());
                        return new ResourceNotFoundException(
                                "Tenant not found with slug: " + request.getTenantSlug());
                    });
        }

        log.debug("Creating Keycloak user for '{}'", request.getUsername());

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

        log.info("User '{}' registered successfully with role '{}'",
                savedUser.getUsername(), savedUser.getRole());

        String token = keycloakTokenService.loginAndGetAccessToken(
                request.getUsername(),
                request.getPassword()
        );

        log.debug("Access token issued for user '{}'", savedUser.getUsername());

        return buildAuthResponse(savedUser, token);
    }

    public AuthResponse login(LoginRequest request) {

        log.info("Login request received for username='{}'", request.getUsername());

        String token = keycloakTokenService.loginAndGetAccessToken(
                request.getUsername(),
                request.getPassword()
        );

        log.debug("Login successful with Keycloak for '{}'", request.getUsername());

        Jwt jwt = decodeJwt(token);

        User user = userIdentityService.resolveOrProvisionUserFromJwt(jwt);

        log.info("User '{}' authenticated successfully with role '{}'",
                user.getUsername(), user.getRole());

        return buildAuthResponse(user, token);
    }

    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return Optional.of((User) authentication.getPrincipal());
        }
        return Optional.empty();
    }

    private Jwt decodeJwt(String token) {

        try {
            log.debug("Decoding JWT using JwtDecoder.");

            return jwtDecoder.decode(token);

        } catch (Exception ex) {

            log.warn("JwtDecoder failed. Falling back to unverified JWT parsing.");

            return parseJwtUnverified(token);
        }
    }

    private Jwt parseJwtUnverified(String token) {

        try {

            String[] parts = token.split("\\.");

            if (parts.length >= 2) {

                String payloadJson = new String(
                        java.util.Base64.getUrlDecoder().decode(parts[1]),
                        java.nio.charset.StandardCharsets.UTF_8
                );

                ObjectMapper mapper = new ObjectMapper();

                java.util.Map<String, Object> claims =
                        mapper.readValue(
                                payloadJson,
                                new com.fasterxml.jackson.core.type.TypeReference<>() {}
                        );

                java.time.Instant iat = java.time.Instant.now();
                java.time.Instant exp = iat.plusSeconds(3600);

                if (claims.get("iat") instanceof Number n) {
                    iat = java.time.Instant.ofEpochSecond(n.longValue());
                }

                if (claims.get("exp") instanceof Number n) {
                    exp = java.time.Instant.ofEpochSecond(n.longValue());
                }

                log.debug("JWT parsed successfully without signature verification.");

                return new Jwt(
                        token,
                        iat,
                        exp,
                        java.util.Map.of("alg", "none"),
                        claims
                );
            }

        } catch (Exception ex) {

            log.error("Failed to parse JWT.", ex);
        }

        throw new BadRequestException("Unable to decode authentication token");
    }

    private AuthResponse buildAuthResponse(User user, String token) {

        Tenant tenant = user.getTenant();

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .tenantId(tenant != null ? tenant.getId() : null)
                .tenantSlug(tenant != null ? tenant.getSlug() : null)
                .tenantName(tenant != null ? tenant.getName() : null)
                .build();
    }
}
