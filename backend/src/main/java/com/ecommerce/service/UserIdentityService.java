package com.ecommerce.service;

import com.ecommerce.exception.BadRequestException;
import com.ecommerce.model.Role;
import com.ecommerce.model.Tenant;
import com.ecommerce.model.User;
import com.ecommerce.repository.TenantRepository;
import com.ecommerce.repository.UserRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Service
public class UserIdentityService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final KeycloakAdminService keycloakAdminService;

    public UserIdentityService(
            UserRepository userRepository,
            TenantRepository tenantRepository,
            KeycloakAdminService keycloakAdminService) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.keycloakAdminService = keycloakAdminService;
    }

    @Transactional
    public User resolveOrProvisionUserFromJwt(Jwt jwt) {
        String keycloakUserId = jwt.getSubject();
        String username = trimToNull(jwt.getClaimAsString("preferred_username"));
        String email = trimToNull(jwt.getClaimAsString("email"));
        String name = trimToNull(jwt.getClaimAsString("name"));

        User existing = findExistingUser(keycloakUserId, username, email);
        Role mappedRole = extractRole(jwt);
        Tenant tenant = resolveTenant(jwt, mappedRole, existing, username);

        if (existing != null) {
            existing.setKeycloakUserId(keycloakUserId);
            if (mappedRole != Role.USER || existing.getRole() == null) {
                existing.setRole(mappedRole);
            }
            if (tenant != null) {
                existing.setTenant(tenant);
            }
            if (name != null) {
                existing.setName(name);
            }
            if (username != null) {
                existing.setUsername(username);
            }
            if (email != null) {
                existing.setEmail(email);
            }
            return userRepository.save(existing);
        }

        String effectiveUsername = username != null ? username : keycloakUserId;
        String effectiveEmail = email != null ? email : effectiveUsername + "@keycloak.local";
        String effectiveName = name != null ? name : effectiveUsername;

        User user = User.builder()
                .name(effectiveName)
                .username(effectiveUsername)
                .email(effectiveEmail)
                .password(null)
                .keycloakUserId(keycloakUserId)
                .role(mappedRole)
                .tenant(tenant)
                .build();

        return userRepository.save(user);
    }

    private User findExistingUser(String keycloakUserId, String username, String email) {
        if (keycloakUserId != null) {
            Optional<User> byKeycloakId = userRepository.findByKeycloakUserId(keycloakUserId);
            if (byKeycloakId.isPresent()) {
                return byKeycloakId.get();
            }
        }
        if (username != null) {
            Optional<User> byUsername = userRepository.findByUsername(username);
            if (byUsername.isPresent()) {
                return byUsername.get();
            }
        }
        if (email != null) {
            return userRepository.findByEmail(email).orElse(null);
        }
        return null;
    }

    private Tenant resolveTenant(
            Jwt jwt,
            Role role,
            User existing,
            String username) {
        String tenantSlug = trimToNull(jwt.getClaimAsString("tenantSlug"));
        if (tenantSlug == null) {
            tenantSlug = trimToNull(jwt.getClaimAsString("tenant"));
        }
        if (tenantSlug == null && existing != null && existing.getTenant() != null) {
            tenantSlug = existing.getTenant().getSlug();
        }
        if (tenantSlug == null && role == Role.TENANT_ADMIN) {
            tenantSlug = keycloakAdminService.findTenantSlugByUsername(username);
        }
        if (tenantSlug == null) {
            if (role == Role.TENANT_ADMIN) {
                throw new BadRequestException("TENANT_ADMIN token must include tenantSlug or tenant claim");
            }
            return null;
        }
        final String tenantKey = tenantSlug;
        return tenantRepository.findBySlugIgnoreCase(tenantKey)
            .orElseThrow(() -> new BadRequestException("Tenant not found with slug: " + tenantKey));
    }

    @SuppressWarnings("unchecked")
    private Role extractRole(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.get("roles") instanceof Collection<?> roles) {
            if (roles.stream().map(Object::toString).anyMatch(role -> role.equalsIgnoreCase("ADMIN"))) {
                return Role.ADMIN;
            }
            if (roles.stream().map(Object::toString).anyMatch(role -> role.equalsIgnoreCase("TENANT_ADMIN"))) {
                return Role.TENANT_ADMIN;
            }
        }
        String username = trimToNull(jwt.getClaimAsString("preferred_username"));
        if (username != null && (username.equalsIgnoreCase("adminuser") || username.equalsIgnoreCase("platform_admin"))) {
            return Role.ADMIN;
        }
        return Role.USER;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
