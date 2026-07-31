package com.ecommerce.service;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.OrderItemRequest;
import com.ecommerce.dto.OrderResponse;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.model.*;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private TenantService tenantService;

    @InjectMocks
    private OrderService orderService;

    private Tenant nikeTenant;
    private User customer;
    private Product nikeShoe;
    private CreateOrderRequest createOrderRequest;

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
                .price(100.0)
                .category("Footwear")
                .availableQuantity(10)
                .build();

        OrderItemRequest itemReq = OrderItemRequest.builder()
                .productId(101L)
                .quantity(2)
                .build();

        createOrderRequest = CreateOrderRequest.builder()
                .items(Collections.singletonList(itemReq))
                .build();
    }

    @Test
    void testCreateOrder_Success() {
        when(tenantService.getTenantEntityBySlug("nike")).thenReturn(nikeTenant);
        when(productRepository.findByIdAndTenantId(101L, 1L)).thenReturn(Optional.of(nikeShoe));

        Order savedOrder = Order.builder()
                .id(500L)
                .user(customer)
                .tenant(nikeTenant)
                .orderDate(LocalDateTime.now())
                .totalQuantity(2)
                .totalAmount(200.0)
                .status(OrderStatus.COMPLETED)
                .items(Collections.emptyList())
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderResponse response = orderService.createOrder("nike", createOrderRequest, customer);

        assertNotNull(response);
        assertEquals(OrderStatus.COMPLETED, response.getStatus());
        assertEquals(8, nikeShoe.getAvailableQuantity());
        verify(productRepository, times(1)).save(nikeShoe);
    }

    @Test
    void testCreateOrder_InsufficientStock_ThrowsException() {
        nikeShoe.setAvailableQuantity(1);

        when(tenantService.getTenantEntityBySlug("nike")).thenReturn(nikeTenant);
        when(productRepository.findByIdAndTenantId(101L, 1L)).thenReturn(Optional.of(nikeShoe));

        assertThrows(InsufficientStockException.class, () ->
                orderService.createOrder("nike", createOrderRequest, customer));

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testCreateOrder_ExactStock_Success() {
        nikeShoe.setAvailableQuantity(2);

        when(tenantService.getTenantEntityBySlug("nike")).thenReturn(nikeTenant);
        when(productRepository.findByIdAndTenantId(101L, 1L)).thenReturn(Optional.of(nikeShoe));

        Order savedOrder = Order.builder()
                .id(501L)
                .user(customer)
                .tenant(nikeTenant)
                .orderDate(LocalDateTime.now())
                .totalQuantity(2)
                .totalAmount(200.0)
                .status(OrderStatus.COMPLETED)
                .items(Collections.emptyList())
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderResponse response = orderService.createOrder("nike", createOrderRequest, customer);
        assertNotNull(response);
        assertEquals(0, nikeShoe.getAvailableQuantity());
    }

    @Test
    void testGetUserOrderHistory_ReturnsOrders() {
        when(orderRepository.findByUserIdOrderByOrderDateDesc(5L)).thenReturn(Collections.emptyList());

        var history = orderService.getUserOrderHistory(customer);

        assertNotNull(history);
        assertTrue(history.isEmpty());
        verify(orderRepository, times(1)).findByUserIdOrderByOrderDateDesc(5L);
    }
}
