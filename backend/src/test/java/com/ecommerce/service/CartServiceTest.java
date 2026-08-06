package com.ecommerce.service;

import com.ecommerce.dto.request.CartItemRequest;
import com.ecommerce.dto.request.UpdateCartItemRequest;
import com.ecommerce.dto.response.CartItemResponse;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.UnauthorizedAccessException;
import com.ecommerce.model.*;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TenantService tenantService;

    @InjectMocks
    private CartService cartService;

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
    void testAddToCart_NewItem_Success() {
        CartItemRequest request = CartItemRequest.builder()
                .productId(101L)
                .quantity(2)
                .build();

        when(tenantService.getTenantEntityBySlug("nike")).thenReturn(nikeTenant);
        when(productRepository.findByIdAndTenantId(101L, 1L)).thenReturn(Optional.of(nikeShoe));
        when(userRepository.findById(5L)).thenReturn(Optional.of(customer));
        when(cartItemRepository.findByUserIdAndProductId(5L, 101L)).thenReturn(Optional.empty());

        CartItem savedItem = CartItem.builder()
                .id(300L)
                .user(customer)
                .product(nikeShoe)
                .quantity(2)
                .build();
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(savedItem);

        CartItemResponse response = cartService.addToCart("nike", request, customer);

        assertNotNull(response);
        assertEquals(300L, response.getId());
        assertEquals(2, response.getQuantity());
        assertEquals(BigDecimal.valueOf(200.0), response.getSubtotal());
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void testAddToCart_ExistingItem_IncrementsQuantity() {
        CartItemRequest request = CartItemRequest.builder()
                .productId(101L)
                .quantity(3)
                .build();

        CartItem existing = CartItem.builder()
                .id(300L)
                .user(customer)
                .product(nikeShoe)
                .quantity(2)
                .build();

        when(tenantService.getTenantEntityBySlug("nike")).thenReturn(nikeTenant);
        when(productRepository.findByIdAndTenantId(101L, 1L)).thenReturn(Optional.of(nikeShoe));
        when(userRepository.findById(5L)).thenReturn(Optional.of(customer));
        when(cartItemRepository.findByUserIdAndProductId(5L, 101L)).thenReturn(Optional.of(existing));

        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartItemResponse response = cartService.addToCart("nike", request, customer);

        assertEquals(5, response.getQuantity());
        assertEquals(BigDecimal.valueOf(500.0), response.getSubtotal());
    }

    @Test
    void testAddToCart_InsufficientStock_ThrowsException() {
        CartItemRequest request = CartItemRequest.builder()
                .productId(101L)
                .quantity(11)
                .build();

        when(tenantService.getTenantEntityBySlug("nike")).thenReturn(nikeTenant);
        when(productRepository.findByIdAndTenantId(101L, 1L)).thenReturn(Optional.of(nikeShoe));

        assertThrows(InsufficientStockException.class, () ->
                cartService.addToCart("nike", request, customer));

        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void testAddToCart_AdminRole_ThrowsException() {
        User admin = User.builder()
                .id(9L)
                .username("adminuser")
                .role(Role.ADMIN)
                .build();

        CartItemRequest request = CartItemRequest.builder()
                .productId(101L)
                .quantity(2)
                .build();

        assertThrows(UnauthorizedAccessException.class, () ->
                cartService.addToCart("nike", request, admin));

        verify(cartItemRepository, never()).save(any(CartItem.class));
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void testGetCart_ReturnsItems() {
        CartItem existing = CartItem.builder()
                .id(300L)
                .user(customer)
                .product(nikeShoe)
                .quantity(2)
                .build();

        when(cartItemRepository.findByUserIdAndProduct_Tenant_Slug(5L, "nike"))
                .thenReturn(Collections.singletonList(existing));

        var cart = cartService.getCart("nike", customer);

        assertNotNull(cart);
        assertEquals(1, cart.size());
        assertEquals(300L, cart.get(0).getId());
        verify(cartItemRepository, times(1))
                .findByUserIdAndProduct_Tenant_Slug(5L, "nike");
    }

    @Test
    void testGetCart_GlobalSlug_ReturnsAllTenantItems() {
        CartItem existing = CartItem.builder()
                .id(300L)
                .user(customer)
                .product(nikeShoe)
                .quantity(2)
                .build();

        when(cartItemRepository.findByUserId(5L))
                .thenReturn(Collections.singletonList(existing));

        var cart = cartService.getCart("global", customer);

        assertNotNull(cart);
        assertEquals(1, cart.size());
        assertEquals(300L, cart.get(0).getId());
        verify(cartItemRepository, times(1)).findByUserId(5L);
        verify(cartItemRepository, never())
                .findByUserIdAndProduct_Tenant_Slug(anyLong(), anyString());
    }

    @Test
    void testUpdateQuantity_Success() {
        CartItem existing = CartItem.builder()
                .id(300L)
                .user(customer)
                .product(nikeShoe)
                .quantity(2)
                .build();

        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
                .quantity(5)
                .build();

        when(cartItemRepository.findByIdAndUserId(300L, 5L)).thenReturn(Optional.of(existing));
        when(productRepository.findByIdAndTenantId(101L, 1L)).thenReturn(Optional.of(nikeShoe));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartItemResponse response = cartService.updateQuantity("nike", 300L, request, customer);

        assertEquals(5, response.getQuantity());
        assertEquals(BigDecimal.valueOf(500.0), response.getSubtotal());
    }

    @Test
    void testUpdateQuantity_InsufficientStock_ThrowsException() {
        CartItem existing = CartItem.builder()
                .id(300L)
                .user(customer)
                .product(nikeShoe)
                .quantity(2)
                .build();

        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
                .quantity(11)
                .build();

        when(cartItemRepository.findByIdAndUserId(300L, 5L)).thenReturn(Optional.of(existing));
        when(productRepository.findByIdAndTenantId(101L, 1L)).thenReturn(Optional.of(nikeShoe));

        assertThrows(InsufficientStockException.class, () ->
                cartService.updateQuantity("nike", 300L, request, customer));
    }

    @Test
    void testUpdateQuantity_ItemNotOwned_ThrowsException() {
        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
                .quantity(5)
                .build();

        when(cartItemRepository.findByIdAndUserId(300L, 5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                cartService.updateQuantity("nike", 300L, request, customer));
    }

    @Test
    void testRemoveItem_Success() {
        CartItem existing = CartItem.builder()
                .id(300L)
                .user(customer)
                .product(nikeShoe)
                .quantity(2)
                .build();

        when(cartItemRepository.findByIdAndUserId(300L, 5L)).thenReturn(Optional.of(existing));

        cartService.removeItem("nike", 300L, customer);

        verify(cartItemRepository, times(1)).delete(existing);
    }

    @Test
    void testClearCart_Success() {
        cartService.clearCart("nike", customer);

        verify(cartItemRepository, times(1))
                .deleteByUserIdAndProduct_Tenant_Slug(5L, "nike");
    }

    @Test
    void testClearCart_GlobalSlug_DeletesAllTenantItems() {
        cartService.clearCart("global", customer);

        verify(cartItemRepository, times(1)).deleteByUserId(5L);
        verify(cartItemRepository, never())
                .deleteByUserIdAndProduct_Tenant_Slug(anyLong(), anyString());
    }
}
