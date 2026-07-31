package com.ecommerce.service;

import com.ecommerce.model.Role;
import com.ecommerce.model.Tenant;
import com.ecommerce.model.User;
import com.ecommerce.repository.TenantRepository;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserIdentityServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private UserIdentityService userIdentityService;

    @Test
    void resolveOrProvisionUserFromJwt_createsTenantAdminUserWithTenant() {
        Tenant tenant = Tenant.builder().id(3L).name("Nike").slug("nike").build();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "kc-123")
                .claim("preferred_username", "nike_admin")
                .claim("email", "admin@nike.com")
                .claim("name", "Nike Admin")
                .claim("tenantSlug", "nike")
                .claim("realm_access", Map.of("roles", List.of("TENANT_ADMIN")))
                .build();

        when(userRepository.findByKeycloakUserId("kc-123")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("nike_admin")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("admin@nike.com")).thenReturn(Optional.empty());
        when(tenantRepository.findBySlugIgnoreCase("nike")).thenReturn(Optional.of(tenant));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = userIdentityService.resolveOrProvisionUserFromJwt(jwt);

        assertEquals("kc-123", user.getKeycloakUserId());
        assertEquals(Role.TENANT_ADMIN, user.getRole());
        assertEquals("nike", user.getTenant().getSlug());
    }

    @Test
    void resolveOrProvisionUserFromJwt_defaultsToUserRoleWithoutRealmRoles() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "kc-999")
                .claim("preferred_username", "john")
                .build();

        when(userRepository.findByKeycloakUserId("kc-999")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = userIdentityService.resolveOrProvisionUserFromJwt(jwt);

        assertEquals(Role.USER, user.getRole());
        assertNull(user.getTenant());
        assertEquals("john@keycloak.local", user.getEmail());
    }
}
