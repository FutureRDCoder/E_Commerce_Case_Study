package com.ecommerce.service;

import com.ecommerce.dto.request.AssignTenantRequest;
import com.ecommerce.dto.response.UserResponse;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Role;
import com.ecommerce.model.Tenant;
import com.ecommerce.model.User;
import com.ecommerce.repository.TenantRepository;
import com.ecommerce.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class PlatformUserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final KeycloakAdminService keycloakAdminService;

    public PlatformUserService(
            UserRepository userRepository,
            TenantRepository tenantRepository,
            KeycloakAdminService keycloakAdminService) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.keycloakAdminService = keycloakAdminService;
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getUsers(Role role, Pageable pageable) {

        log.info("Fetching users with role '{}'.", role);

        Page<User> users = (role != null)
                ? userRepository.findByRole(role, pageable)
                : userRepository.findAll(pageable);

        return users.map(this::mapToResponse);
    }

    @Transactional
    public UserResponse assignTenant(Long userId, AssignTenantRequest request) {

        log.info(
                "Assigning tenant '{}' to user '{}'.",
                request.getTenantId(),
                userId
        );

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId
                ));

        if (user.getRole() == Role.ADMIN) {
            throw new BadRequestException(
                    "A platform admin cannot be assigned a brand."
            );
        }

        Tenant tenant = tenantRepository.findById(request.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tenant not found with id: " + request.getTenantId()
                ));

        if (user.getKeycloakUserId() != null
                && !user.getKeycloakUserId().isBlank()) {

            keycloakAdminService.assignTenantToUser(
                    user.getKeycloakUserId(),
                    tenant.getSlug()
            );
        }

        user.setRole(Role.TENANT_ADMIN);
        user.setTenant(tenant);

        User saved = userRepository.save(user);

        log.info(
                "User '{}' is now TENANT_ADMIN of brand '{}'.",
                saved.getUsername(),
                tenant.getSlug()
        );

        return mapToResponse(saved);
    }

    private UserResponse mapToResponse(User user) {

        Tenant tenant = user.getTenant();

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .tenantId(tenant != null ? tenant.getId() : null)
                .tenantName(tenant != null ? tenant.getName() : null)
                .tenantSlug(tenant != null ? tenant.getSlug() : null)
                .build();
    }
}
