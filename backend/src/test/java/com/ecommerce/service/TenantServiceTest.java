package com.ecommerce.service;

import com.ecommerce.dto.request.TenantRequest;
import com.ecommerce.dto.response.TenantResponse;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Product;
import com.ecommerce.model.Tenant;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.TenantRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.repository.ProductRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartItemRepository cartItemRepository;

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
    void testGetAllTenants_ReturnsAllActive() {

        Tenant adidasTenant = Tenant.builder()
                .id(2L)
                .name("Adidas")
                .slug("adidas")
                .build();

        Page<Tenant> tenantPage = new PageImpl<>(
                List.of(nikeTenant, adidasTenant)
        );

        when(tenantRepository.findByActiveTrue(any(Pageable.class)))
                .thenReturn(tenantPage);

        Page<TenantResponse> response =
                tenantService.getAllTenants(PageRequest.of(0, 10));

        assertEquals(2, response.getTotalElements());

        verify(tenantRepository)
                .findByActiveTrue(any(Pageable.class));
    }

    @Test
    void testGetTenantBySlug_Found() {
        when(tenantRepository.findBySlugIgnoreCaseAndActiveTrue("nike")).thenReturn(Optional.of(nikeTenant));

        TenantResponse response = tenantService.getTenantBySlug("nike");

        assertNotNull(response);
        assertEquals("Nike", response.getName());
    }

    @Test
    void testGetTenantBySlug_NotFound_ThrowsException() {
        when(tenantRepository.findBySlugIgnoreCaseAndActiveTrue("unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tenantService.getTenantBySlug("unknown"));
    }

    @Test
    void testDeleteTenant_Success_DeactivatesWithoutDeletingOrdersOrFavourites() {
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(nikeTenant));

        when(userRepository.findByTenantId(1L))
                .thenReturn(List.of());

        Product nikeProduct = Product.builder()
                .id(100L)
                .tenant(nikeTenant)
                .name("Air Max")
                .price(java.math.BigDecimal.valueOf(150))
                .category("Footwear")
                .availableQuantity(20)
                .build();

        when(productRepository.findByTenantId(1L))
                .thenReturn(List.of(nikeProduct));

        tenantService.deleteTenant(1L);

        assertFalse(nikeTenant.isActive());
        assertFalse(nikeProduct.isActive());

        verify(tenantRepository, times(1)).save(nikeTenant);
        verify(tenantRepository, never()).delete(any());

        verify(productRepository, times(1)).save(nikeProduct);
        verify(cartItemRepository, times(1)).deleteByProductId(100L);
    }

    @Test
    void testDeleteTenant_NotFound_ThrowsException() {
        when(tenantRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tenantService.deleteTenant(99L));
    }
}
