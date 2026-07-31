package com.ecommerce.controller;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.OrderResponse;
import com.ecommerce.model.User;
import com.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/{tenantSlug}/orders")
public class MultiTenantOrderController {

    private final OrderService orderService;

    public MultiTenantOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @PathVariable String tenantSlug,
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(tenantSlug, request, currentUser));
    }

    @GetMapping("/my-history")
    public ResponseEntity<List<OrderResponse>> getMyOrderHistory(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(orderService.getUserOrderHistory(currentUser));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<List<OrderResponse>> getTenantOrders(
            @PathVariable String tenantSlug,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(orderService.getTenantOrders(tenantSlug, currentUser));
    }
}
