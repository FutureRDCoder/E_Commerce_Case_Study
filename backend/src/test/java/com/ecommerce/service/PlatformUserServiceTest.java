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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PlatformUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private KeycloakAdminService keycloakAdminService;

    @InjectMocks
    private PlatformUserService platformUserService;

    private User normalUser;
    private Tenant nikeTenant;

    @BeforeEach
    void setUp() {
        nikeTenant = Tenant.builder()
                .id(1L)
                .name("Nike")
                .slug("nike")
                .build();

        normalUser = User.builder()
                .id(1L)
                .name("Test User")
                .username("testuser")
                .email("test@example.com")
                .keycloakUserId("kc-user-1")
                .role(Role.USER)
                .build();
    }

    @Test
    void testGetUsers_FiltersByRole() {
        Page<User> userPage = new PageImpl<>(List.of(normalUser));

        when(userRepository.findByRole(Role.USER, PageRequest.of(0, 100)))
                .thenReturn(userPage);

        Page<UserResponse> response =
                platformUserService.getUsers(Role.USER, PageRequest.of(0, 100));

        assertEquals(1, response.getTotalElements());
        assertEquals(Role.USER, response.getContent().get(0).getRole());
        verify(userRepository).findByRole(eq(Role.USER), any(Pageable.class));
    }

    @Test
    void testAssignTenant_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(nikeTenant));
        when(userRepository.save(any(User.class))).thenReturn(normalUser);

        AssignTenantRequest request = AssignTenantRequest.builder()
                .tenantId(1L)
                .build();

        UserResponse response = platformUserService.assignTenant(1L, request);

        assertNotNull(response);
        assertEquals(Role.TENANT_ADMIN, response.getRole());
        assertEquals(1L, response.getTenantId());
        assertEquals("nike", response.getTenantSlug());

        verify(keycloakAdminService).assignTenantToUser("kc-user-1", "nike");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testAssignTenant_UserNotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        AssignTenantRequest request = AssignTenantRequest.builder()
                .tenantId(1L)
                .build();

        assertThrows(
                ResourceNotFoundException.class,
                () -> platformUserService.assignTenant(99L, request)
        );
        verify(userRepository, never()).save(any());
    }

    @Test
    void testAssignTenant_TenantNotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));
        when(tenantRepository.findById(99L)).thenReturn(Optional.empty());

        AssignTenantRequest request = AssignTenantRequest.builder()
                .tenantId(99L)
                .build();

        assertThrows(
                ResourceNotFoundException.class,
                () -> platformUserService.assignTenant(1L, request)
        );
        verify(userRepository, never()).save(any());
    }

    @Test
    void testAssignTenant_AdminUser_ThrowsException() {
        User admin = User.builder()
                .id(2L)
                .name("Admin")
                .username("adminuser")
                .email("admin@example.com")
                .role(Role.ADMIN)
                .build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(admin));

        AssignTenantRequest request = AssignTenantRequest.builder()
                .tenantId(1L)
                .build();

        assertThrows(
                BadRequestException.class,
                () -> platformUserService.assignTenant(2L, request)
        );
        verify(userRepository, never()).save(any());
    }
}
