package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.*;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final TenantService tenantService;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, TenantService tenantService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.tenantService = tenantService;
    }

    @Transactional
    public OrderResponse createOrder(String tenantSlug, CreateOrderRequest request, User currentUser) {
        Tenant tenant = tenantService.getTenantEntityBySlug(tenantSlug);

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Order must contain at least one order item");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        int totalQuantity = 0;
        double totalAmount = 0.0;

        Order order = Order.builder()
                .user(currentUser)
                .tenant(tenant)
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.COMPLETED)
                .build();

        for (OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findByIdAndTenantId(itemReq.getProductId(), tenant.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product with id " + itemReq.getProductId() + " not found under brand " + tenantSlug));

            if (itemReq.getQuantity() > product.getAvailableQuantity()) {
                throw new InsufficientStockException("Cannot order " + itemReq.getQuantity() + " units of " + product.getName() + ". Available stock: " + product.getAvailableQuantity());
            }

            product.setAvailableQuantity(product.getAvailableQuantity() - itemReq.getQuantity());
            productRepository.save(product);

            double subtotal = product.getPrice() * itemReq.getQuantity();
            totalQuantity += itemReq.getQuantity();
            totalAmount += subtotal;

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(product.getPrice())
                    .subtotal(subtotal)
                    .build();

            orderItems.add(orderItem);
        }

        order.setTotalQuantity(totalQuantity);
        order.setTotalAmount(totalAmount);
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);
        return mapToResponse(savedOrder);
    }

    public List<OrderResponse> getUserOrderHistory(User currentUser) {
        return orderRepository.findByUserIdOrderByOrderDateDesc(currentUser.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getTenantOrders(String tenantSlug, User currentUser) {
        Tenant tenant = tenantService.getTenantEntityBySlug(tenantSlug);
        return orderRepository.findByTenantIdOrderByOrderDateDesc(tenant.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .productCategory(item.getProduct().getCategory())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .productImageUrl(item.getProduct().getImageUrl())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .username(order.getUser().getUsername())
                .userFullName(order.getUser().getName())
                .tenantId(order.getTenant().getId())
                .tenantName(order.getTenant().getName())
                .tenantSlug(order.getTenant().getSlug())
                .orderDate(order.getOrderDate())
                .totalQuantity(order.getTotalQuantity())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .items(itemResponses)
                .build();
    }
}
