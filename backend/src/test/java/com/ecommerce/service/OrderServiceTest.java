package com.ecommerce.service;

import com.ecommerce.dto.request.CreateOrderRequest;
import com.ecommerce.dto.request.OrderItemRequest;
import com.ecommerce.dto.response.OrderResponse;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.UnauthorizedAccessException;
import com.ecommerce.model.*;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
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
    private UserRepository userRepository;

    @Mock
    private TenantService tenantService;

    @Mock
    private ProductService productService;

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
                .price(BigDecimal.valueOf(100.0))
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
        when(userRepository.findById(5L)).thenReturn(Optional.of(customer));
        when(productRepository.findByIdAndTenantId(101L, 1L)).thenReturn(Optional.of(nikeShoe));

        Order savedOrder = Order.builder()
                .id(500L)
                .user(customer)
                .tenant(nikeTenant)
                .orderDate(LocalDateTime.now())
                .totalQuantity(2)
                .totalAmount(BigDecimal.valueOf(200.0))
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
        when(userRepository.findById(5L)).thenReturn(Optional.of(customer));
        when(productRepository.findByIdAndTenantId(101L, 1L)).thenReturn(Optional.of(nikeShoe));

        assertThrows(InsufficientStockException.class, () ->
                orderService.createOrder("nike", createOrderRequest, customer));

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testCreateOrder_ExactStock_Success() {
        nikeShoe.setAvailableQuantity(2);

        when(tenantService.getTenantEntityBySlug("nike")).thenReturn(nikeTenant);
        when(userRepository.findById(5L)).thenReturn(Optional.of(customer));
        when(productRepository.findByIdAndTenantId(101L, 1L)).thenReturn(Optional.of(nikeShoe));

        Order savedOrder = Order.builder()
                .id(501L)
                .user(customer)
                .tenant(nikeTenant)
                .orderDate(LocalDateTime.now())
                .totalQuantity(2)
                .totalAmount(BigDecimal.valueOf(200.0))
                .status(OrderStatus.COMPLETED)
                .items(Collections.emptyList())
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderResponse response = orderService.createOrder("nike", createOrderRequest, customer);
        assertNotNull(response);
        assertEquals(0, nikeShoe.getAvailableQuantity());
    }

    @Test
    void testCreateOrder_AdminRole_ThrowsException() {
        User admin = User.builder()
                .id(9L)
                .username("adminuser")
                .role(Role.ADMIN)
                .build();

        assertThrows(UnauthorizedAccessException.class, () ->
                orderService.createOrder("nike", createOrderRequest, admin));

        verify(orderRepository, never()).save(any(Order.class));
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void testGetAllOrders_ReturnsAllOrders() {
        Order savedOrder = Order.builder()
                .id(500L)
                .user(customer)
                .tenant(nikeTenant)
                .orderDate(LocalDateTime.now())
                .totalQuantity(2)
                .totalAmount(BigDecimal.valueOf(200.0))
                .status(OrderStatus.COMPLETED)
                .items(Collections.emptyList())
                .build();

        when(orderRepository.findAllByOrderByOrderDateDesc())
                .thenReturn(Collections.singletonList(savedOrder));

        var orders = orderService.getAllOrders();

        assertNotNull(orders);
        assertEquals(1, orders.size());
        assertEquals(500L, orders.get(0).getId());
        verify(orderRepository, times(1)).findAllByOrderByOrderDateDesc();
    }

    @Test
    void testGetUserOrderHistory_ReturnsOrders() {
        when(orderRepository.findByUserIdAndTenantSlugOrderByOrderDateDesc(5L, "nike"))
                .thenReturn(Collections.emptyList());

        var history = orderService.getUserOrderHistory("nike", customer);

        assertNotNull(history);
        assertTrue(history.isEmpty());
        verify(orderRepository, times(1))
                .findByUserIdAndTenantSlugOrderByOrderDateDesc(5L, "nike");
    }

    @Test
    void testGetUserOrderHistory_GlobalSlug_ReturnsAllOrders() {
        Order savedOrder = Order.builder()
                .id(500L)
                .user(customer)
                .tenant(nikeTenant)
                .orderDate(LocalDateTime.now())
                .totalQuantity(2)
                .totalAmount(BigDecimal.valueOf(200.0))
                .status(OrderStatus.COMPLETED)
                .items(Collections.emptyList())
                .build();

        when(orderRepository.findByUserIdOrderByOrderDateDesc(5L))
                .thenReturn(Collections.singletonList(savedOrder));

        var history = orderService.getUserOrderHistory("global", customer);

        assertNotNull(history);
        assertEquals(1, history.size());
        assertEquals(500L, history.get(0).getId());
        verify(orderRepository, times(1)).findByUserIdOrderByOrderDateDesc(5L);
        verify(orderRepository, never())
                .findByUserIdAndTenantSlugOrderByOrderDateDesc(anyLong(), anyString());
    }

    @Test
    void testCreateOrder_GlobalSlug_InfersTenantFromProduct() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(101L)).thenReturn(Optional.of(nikeShoe));
        when(productRepository.findByIdAndTenantId(101L, 1L)).thenReturn(Optional.of(nikeShoe));

        Order savedOrder = Order.builder()
                .id(600L)
                .user(customer)
                .tenant(nikeTenant)
                .orderDate(LocalDateTime.now())
                .totalQuantity(2)
                .totalAmount(BigDecimal.valueOf(200.0))
                .status(OrderStatus.COMPLETED)
                .items(Collections.emptyList())
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderResponse response = orderService.createOrder("global", createOrderRequest, customer);

        assertNotNull(response);
        assertEquals(OrderStatus.COMPLETED, response.getStatus());
        assertEquals(8, nikeShoe.getAvailableQuantity());
        verify(tenantService, never()).getTenantEntityBySlug(anyString());
        verify(productRepository, times(1)).save(nikeShoe);
    }

    @Test
    void testCreateOrder_GlobalSlug_MultipleBrands_ThrowsException() {
        Tenant adidasTenant = Tenant.builder().id(2L).name("Adidas").slug("adidas").build();
        Product adidasShoe = Product.builder()
                .id(202L)
                .tenant(adidasTenant)
                .name("Samba")
                .price(BigDecimal.valueOf(100.0))
                .category("Footwear")
                .availableQuantity(10)
                .build();

        OrderItemRequest secondItem = OrderItemRequest.builder()
                .productId(202L)
                .quantity(1)
                .build();

        createOrderRequest = CreateOrderRequest.builder()
                .items(List.of(createOrderRequest.getItems().get(0), secondItem))
                .build();

        when(productRepository.findById(101L)).thenReturn(Optional.of(nikeShoe));
        when(productRepository.findById(202L)).thenReturn(Optional.of(adidasShoe));

        assertThrows(BadRequestException.class, () ->
                orderService.createOrder("global", createOrderRequest, customer));

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testCreateOrder_GlobalSlug_UnknownProducts_ThrowsException() {
        OrderItemRequest unknownItem = OrderItemRequest.builder()
                .productId(999L)
                .quantity(1)
                .build();

        createOrderRequest = CreateOrderRequest.builder()
                .items(Collections.singletonList(unknownItem))
                .build();

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                orderService.createOrder("global", createOrderRequest, customer));

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testGetTenantOrders_OwnTenantAdmin_ReturnsOrders() {
        User nikeAdmin = User.builder()
                .id(10L)
                .username("nike_admin")
                .role(Role.TENANT_ADMIN)
                .tenant(nikeTenant)
                .build();

        Order savedOrder = Order.builder()
                .id(500L)
                .user(customer)
                .tenant(nikeTenant)
                .orderDate(LocalDateTime.now())
                .totalQuantity(2)
                .totalAmount(BigDecimal.valueOf(200.0))
                .status(OrderStatus.COMPLETED)
                .items(Collections.emptyList())
                .build();

        when(tenantService.getTenantEntityBySlug("nike")).thenReturn(nikeTenant);
        doNothing().when(productService).validateTenantAccess(nikeAdmin, nikeTenant);
        when(orderRepository.findByTenantIdOrderByOrderDateDesc(1L))
                .thenReturn(Collections.singletonList(savedOrder));

        var orders = orderService.getTenantOrders("nike", nikeAdmin);

        assertNotNull(orders);
        assertEquals(1, orders.size());
        assertEquals(500L, orders.get(0).getId());
        verify(productService, times(1)).validateTenantAccess(nikeAdmin, nikeTenant);
    }

    @Test
    void testGetTenantOrders_WrongTenantAdmin_ThrowsUnauthorized() {
        Tenant adidasTenant = Tenant.builder().id(2L).name("Adidas").slug("adidas").build();
        User adidasAdmin = User.builder()
                .id(11L)
                .username("adidas_admin")
                .role(Role.TENANT_ADMIN)
                .tenant(adidasTenant)
                .build();

        when(tenantService.getTenantEntityBySlug("nike")).thenReturn(nikeTenant);
        doThrow(new UnauthorizedAccessException(
                "Tenant user cannot perform management operations on another tenant's domain: nike"))
                .when(productService).validateTenantAccess(adidasAdmin, nikeTenant);

        assertThrows(UnauthorizedAccessException.class, () ->
                orderService.getTenantOrders("nike", adidasAdmin));

        verify(orderRepository, never()).findByTenantIdOrderByOrderDateDesc(anyLong());
    }

    @Test
    void testGetTenantOrders_PlatformAdmin_ReturnsOrders() {
        User platformAdmin = User.builder()
                .id(1L)
                .username("admin")
                .role(Role.ADMIN)
                .build();

        Order savedOrder = Order.builder()
                .id(500L)
                .user(customer)
                .tenant(nikeTenant)
                .orderDate(LocalDateTime.now())
                .totalQuantity(2)
                .totalAmount(BigDecimal.valueOf(200.0))
                .status(OrderStatus.COMPLETED)
                .items(Collections.emptyList())
                .build();

        when(tenantService.getTenantEntityBySlug("nike")).thenReturn(nikeTenant);
        doNothing().when(productService).validateTenantAccess(platformAdmin, nikeTenant);
        when(orderRepository.findByTenantIdOrderByOrderDateDesc(1L))
                .thenReturn(Collections.singletonList(savedOrder));

        var orders = orderService.getTenantOrders("nike", platformAdmin);

        assertNotNull(orders);
        assertEquals(1, orders.size());
        verify(productService, times(1)).validateTenantAccess(platformAdmin, nikeTenant);
    }
}
