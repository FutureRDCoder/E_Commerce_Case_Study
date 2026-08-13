package com.ecommerce.service;

import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.dto.request.ProductSearchRequest;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.dto.request.StockUpdateRequest;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.UnauthorizedAccessException;
import com.ecommerce.model.Product;
import com.ecommerce.model.Role;
import com.ecommerce.model.Tenant;
import com.ecommerce.model.User;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.FavouriteProductRepository;
import com.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private TenantService tenantService;

    @Mock
    private FavouriteProductRepository favouriteProductRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private ProductService productService;

    private Tenant nikeTenant;
    private Tenant adidasTenant;
    private User nikeAdminUser;
    private User adidasAdminUser;
    private User platformAdmin;
    private Product nikeProduct;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        nikeTenant = Tenant.builder().id(1L).name("Nike").slug("nike").build();
        adidasTenant = Tenant.builder().id(2L).name("Adidas").slug("adidas").build();

        nikeAdminUser = User.builder()
                .id(10L)
                .username("nike_admin")
                .role(Role.TENANT_ADMIN)
                .tenant(nikeTenant)
                .build();

        adidasAdminUser = User.builder()
                .id(11L)
                .username("adidas_admin")
                .role(Role.TENANT_ADMIN)
                .tenant(adidasTenant)
                .build();

        platformAdmin = User.builder()
                .id(1L)
                .username("admin")
                .role(Role.ADMIN)
                .build();

        nikeProduct = Product.builder()
                .id(100L)
                .tenant(nikeTenant)
                .name("Air Max")
                .price(BigDecimal.valueOf(150.0))
                .category("Footwear")
                .availableQuantity(20)
                .build();

        productRequest = ProductRequest.builder()
                .name("Air Max")
                .price(BigDecimal.valueOf(150.0))
                .category("Footwear")
                .availableQuantity(20)
                .build();
    }

    @Test
    void testAddProduct_ByTenantAdmin_Success() {
        when(tenantService.getTenantEntityBySlug("nike")).thenReturn(nikeTenant);
        when(productRepository.save(any(Product.class))).thenReturn(nikeProduct);

        ProductResponse response = productService.addProduct("nike", productRequest, nikeAdminUser);

        assertNotNull(response);
        assertEquals("Air Max", response.getName());
        assertEquals("nike", response.getTenantSlug());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testAddProduct_ByPlatformAdmin_Success() {
        when(tenantService.getTenantEntityBySlug("nike")).thenReturn(nikeTenant);
        when(productRepository.save(any(Product.class))).thenReturn(nikeProduct);

        ProductResponse response = productService.addProduct("nike", productRequest, platformAdmin);

        assertNotNull(response);
        assertEquals("Air Max", response.getName());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testAddProduct_WrongTenantAdmin_ThrowsUnauthorized() {
        when(tenantService.getTenantEntityBySlug("nike")).thenReturn(nikeTenant);

        assertThrows(UnauthorizedAccessException.class, () ->
                productService.addProduct("nike", productRequest, adidasAdminUser));

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testUpdateStock_Success() {
        StockUpdateRequest stockReq = StockUpdateRequest.builder().availableQuantity(50).build();

        when(tenantService.getTenantEntityBySlug("nike")).thenReturn(nikeTenant);
        when(productRepository.findByIdAndTenantId(100L, 1L)).thenReturn(Optional.of(nikeProduct));
        when(productRepository.save(any(Product.class))).thenReturn(nikeProduct);

        productService.updateStock("nike", 100L, stockReq, nikeAdminUser);

        assertEquals(50, nikeProduct.getAvailableQuantity());
        verify(productRepository, times(1)).save(nikeProduct);
    }

    @Test
    void testDeleteProduct_WrongTenantAdmin_ThrowsUnauthorized() {
        when(tenantService.getTenantEntityBySlug("nike")).thenReturn(nikeTenant);

        assertThrows(UnauthorizedAccessException.class, () ->
                productService.deleteProduct("nike", 100L, adidasAdminUser));

        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    void testDeleteProduct_Success_DeactivatesWithoutDeletingFavouritesOrOrders() {
        when(tenantService.getTenantEntityBySlug("nike")).thenReturn(nikeTenant);
        when(productRepository.findByIdAndTenantId(100L, 1L)).thenReturn(Optional.of(nikeProduct));
        when(productRepository.save(any(Product.class))).thenReturn(nikeProduct);

        productService.deleteProduct("nike", 100L, nikeAdminUser);

        assertFalse(nikeProduct.isActive());
        verify(productRepository, times(1)).save(nikeProduct);
        verify(productRepository, never()).delete(any(Product.class));
        verify(cartItemRepository, times(1)).deleteByProductId(100L);
    }

    @Test
    void testGetProductById_InactiveProduct_ThrowsNotFound() {
        nikeProduct.setActive(false);

        when(tenantService.getTenantEntityBySlug("nike")).thenReturn(nikeTenant);
        when(productRepository.findByIdAndTenantId(100L, 1L)).thenReturn(Optional.of(nikeProduct));

        assertThrows(ResourceNotFoundException.class, () ->
                productService.getProductById("nike", 100L, null));
    }

    @Test
    void testValidateTenantAccess_PlatformAdmin_Passes() {
        assertDoesNotThrow(() -> productService.validateTenantAccess(platformAdmin, nikeTenant));
    }

    @Test
    void testValidateTenantAccess_SameTenantAdmin_Passes() {
        assertDoesNotThrow(() -> productService.validateTenantAccess(nikeAdminUser, nikeTenant));
    }

    @Test
    void testValidateTenantAccess_DifferentTenantAdmin_Throws() {
        assertThrows(UnauthorizedAccessException.class, () ->
                productService.validateTenantAccess(adidasAdminUser, nikeTenant));
    }

    @Test
    void testGetAllProducts_WithCategoryAndPrice_FiltersByPrice() {
        ProductSearchRequest request = ProductSearchRequest.builder()
                .category("Footwear")
                .minPrice(BigDecimal.valueOf(100))
                .maxPrice(BigDecimal.valueOf(200))
                .build();

        Page<Product> productPage = new PageImpl<>(List.of(nikeProduct));
        when(productRepository.searchProducts(
                isNull(),
                eq("Footwear"),
                isNull(),
                eq(BigDecimal.valueOf(100)),
                eq(BigDecimal.valueOf(200)),
                any()
        )).thenReturn(productPage);

        Page<ProductResponse> response = productService.getAllProducts(request, null);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        verify(productRepository).searchProducts(
                isNull(),
                eq("Footwear"),
                isNull(),
                eq(BigDecimal.valueOf(100)),
                eq(BigDecimal.valueOf(200)),
                any()
        );
    }

    @Test
    void testGetAllProducts_OnlyMaxPrice_AppliesUpperBound() {
        ProductSearchRequest request = ProductSearchRequest.builder()
                .maxPrice(BigDecimal.valueOf(150))
                .build();

        Page<Product> productPage = new PageImpl<>(List.of());
        when(productRepository.searchProducts(
                isNull(), isNull(), isNull(),
                isNull(), eq(BigDecimal.valueOf(150)), any()
        )).thenReturn(productPage);

        productService.getAllProducts(request, null);

        verify(productRepository).searchProducts(
                isNull(), isNull(), isNull(),
                isNull(), eq(BigDecimal.valueOf(150)), any()
        );
    }

    @Test
    void testGetProducts_WithPriceFilter_ScopesToTenant() {
        ProductSearchRequest request = ProductSearchRequest.builder()
                .minPrice(BigDecimal.valueOf(50))
                .build();

        Page<Product> productPage = new PageImpl<>(List.of(nikeProduct));
        when(tenantService.getTenantEntityBySlug("nike")).thenReturn(nikeTenant);
        when(productRepository.searchProducts(
                eq(1L), isNull(), isNull(),
                eq(BigDecimal.valueOf(50)), isNull(), any()
        )).thenReturn(productPage);

        productService.getProducts("nike", request, null);

        verify(productRepository).searchProducts(
                eq(1L), isNull(), isNull(),
                eq(BigDecimal.valueOf(50)), isNull(), any()
        );
    }

    @Test
    void testGetAllProducts_MinGreaterThanMax_ThrowsBadRequest() {
        ProductSearchRequest request = ProductSearchRequest.builder()
                .minPrice(BigDecimal.valueOf(500))
                .maxPrice(BigDecimal.valueOf(100))
                .build();

        assertThrows(BadRequestException.class, () ->
                productService.getAllProducts(request, null));

        verify(productRepository, never()).searchProducts(
                any(), any(), any(), any(), any(), any()
        );
    }

    @Test
    void testGetProducts_CategoryBlank_NormalizesToNull() {
        ProductSearchRequest request = ProductSearchRequest.builder()
                .category("  ")
                .search(" Air ")
                .build();

        Page<Product> productPage = new PageImpl<>(List.of());
        when(tenantService.getTenantEntityBySlug("nike")).thenReturn(nikeTenant);
        when(productRepository.searchProducts(
                eq(1L), isNull(), eq("Air"),
                isNull(), isNull(), any()
        )).thenReturn(productPage);

        productService.getProducts("nike", request, null);

        verify(productRepository).searchProducts(
                eq(1L), isNull(), eq("Air"),
                isNull(), isNull(), any()
        );
    }
}
