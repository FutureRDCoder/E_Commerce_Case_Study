package com.ecommerce.service;

import com.ecommerce.dto.TenantRequest;
import com.ecommerce.dto.TenantResponse;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Tenant;
import com.ecommerce.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private TenantService tenantService;

    private Tenant nikeTenant;
    private TenantRequest tenantRequest;

    @BeforeEach
    void setUp() {
        nikeTenant = Tenant.builder()
                .id(1L)
                .name("Nike")
                .slug("nike")
                .description("Just Do It")
                .build();

        tenantRequest = TenantRequest.builder()
                .name("Puma")
                .slug("puma")
                .description("Forever Faster")
                .build();
    }

    @Test
    void testCreateTenant_Success() {
        when(tenantRepository.existsBySlugIgnoreCase("puma")).thenReturn(false);
        when(tenantRepository.existsByNameIgnoreCase("Puma")).thenReturn(false);

        Tenant pumaTenant = Tenant.builder().id(3L).name("Puma").slug("puma").description("Forever Faster").build();
        when(tenantRepository.save(any(Tenant.class))).thenReturn(pumaTenant);

        TenantResponse response = tenantService.createTenant(tenantRequest);

        assertNotNull(response);
        assertEquals("Puma", response.getName());
        assertEquals("puma", response.getSlug());
        verify(tenantRepository, times(1)).save(any(Tenant.class));
    }

    @Test
    void testCreateTenant_DuplicateSlug_ThrowsException() {
        when(tenantRepository.existsBySlugIgnoreCase("puma")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> tenantService.createTenant(tenantRequest));
        verify(tenantRepository, never()).save(any(Tenant.class));
    }

    @Test
    void testGetAllTenants_ReturnsAll() {
        Tenant adidasTenant = Tenant.builder().id(2L).name("Adidas").slug("adidas").build();
        when(tenantRepository.findAll()).thenReturn(Arrays.asList(nikeTenant, adidasTenant));

        List<TenantResponse> tenants = tenantService.getAllTenants();

        assertEquals(2, tenants.size());
    }

    @Test
    void testGetTenantBySlug_Found() {
        when(tenantRepository.findBySlugIgnoreCase("nike")).thenReturn(Optional.of(nikeTenant));

        TenantResponse response = tenantService.getTenantBySlug("nike");

        assertNotNull(response);
        assertEquals("Nike", response.getName());
    }

    @Test
    void testGetTenantBySlug_NotFound_ThrowsException() {
        when(tenantRepository.findBySlugIgnoreCase("unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tenantService.getTenantBySlug("unknown"));
    }

    @Test
    void testDeleteTenant_Success() {
        when(tenantRepository.existsById(1L)).thenReturn(true);

        tenantService.deleteTenant(1L);

        verify(tenantRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteTenant_NotFound_ThrowsException() {
        when(tenantRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> tenantService.deleteTenant(99L));
    }
}
