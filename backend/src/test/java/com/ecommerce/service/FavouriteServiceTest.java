package com.ecommerce.service;

import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.FavouriteProduct;
import com.ecommerce.model.Product;
import com.ecommerce.model.Role;
import com.ecommerce.model.Tenant;
import com.ecommerce.model.User;
import com.ecommerce.repository.FavouriteProductRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FavouriteServiceTest {

    @Mock
    private FavouriteProductRepository favouriteProductRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TenantService tenantService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private FavouriteService favouriteService;

    private Tenant nikeTenant;
    private User customer;
    private Product nikeShoe;

    @BeforeEach
    void setUp() {
        nikeTenant = Tenant.builder().id(1L).name("Nike").slug("nike").build();

        customer = User.builder()
                .id(5L)
                .username("customer")
                .role(Role.USER)
                .build();

        nikeShoe = Product.builder()
                .id(101L)
                .tenant(nikeTenant)
                .name("Air Max")
                .price(BigDecimal.valueOf(100.0))
                .category("Footwear")
                .availableQuantity(10)
                .imageUrl("https://example.com/shoe.png")
                .build();
    }

    @Test
    void testGetUserFavourites_TenantSlug_FiltersByTenant() {
        FavouriteProduct favourite = FavouriteProduct.builder()
                .id(1L)
                .user(customer)
                .product(nikeShoe)
                .build();

        when(favouriteProductRepository.findByUserIdAndProduct_Tenant_Slug(5L, "nike"))
                .thenReturn(List.of(favourite));
        when(productService.mapToResponse(nikeShoe, customer))
                .thenReturn(ProductResponse.builder().id(101L).build());

        var favourites = favouriteService.getUserFavourites("nike", customer);

        assertNotNull(favourites);
        assertEquals(1, favourites.size());
        assertEquals(101L, favourites.get(0).getId());
        verify(favouriteProductRepository, times(1))
                .findByUserIdAndProduct_Tenant_Slug(5L, "nike");
    }

    @Test
    void testGetUserFavourites_GlobalSlug_ReturnsAllTenantItems() {
        FavouriteProduct favourite = FavouriteProduct.builder()
                .id(1L)
                .user(customer)
                .product(nikeShoe)
                .build();

        when(favouriteProductRepository.findByUserId(5L))
                .thenReturn(List.of(favourite));
        when(productService.mapToResponse(nikeShoe, customer))
                .thenReturn(ProductResponse.builder().id(101L).build());

        var favourites = favouriteService.getUserFavourites("global", customer);

        assertNotNull(favourites);
        assertEquals(1, favourites.size());
        assertEquals(101L, favourites.get(0).getId());
        verify(favouriteProductRepository, times(1)).findByUserId(5L);
        verify(favouriteProductRepository, never())
                .findByUserIdAndProduct_Tenant_Slug(anyLong(), anyString());
    }

    @Test
    void testAddFavourite_AlreadyExists_ThrowsException() {
        when(tenantService.getTenantEntityBySlug("nike")).thenReturn(nikeTenant);
        when(productRepository.findByIdAndTenantId(101L, 1L)).thenReturn(Optional.of(nikeShoe));
        when(favouriteProductRepository.existsByUserIdAndProductId(5L, 101L)).thenReturn(true);

        assertThrows(BadRequestException.class, () ->
                favouriteService.addFavourite("nike", 101L, customer));

        verify(favouriteProductRepository, never()).save(any());
    }

    @Test
    void testAddFavourite_ProductNotFound_ThrowsException() {
        when(tenantService.getTenantEntityBySlug("nike")).thenReturn(nikeTenant);
        when(productRepository.findByIdAndTenantId(999L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                favouriteService.addFavourite("nike", 999L, customer));
    }
}
