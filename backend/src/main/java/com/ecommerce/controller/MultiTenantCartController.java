package com.ecommerce.controller;

import com.ecommerce.dto.request.CartItemRequest;
import com.ecommerce.dto.request.UpdateCartItemRequest;
import com.ecommerce.dto.response.CartItemResponse;
import com.ecommerce.model.User;
import com.ecommerce.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/{tenantSlug}/cart")
public class MultiTenantCartController {

    private final CartService cartService;

    public MultiTenantCartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<List<CartItemResponse>> getCart(
            @PathVariable String tenantSlug,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
                cartService.getCart(tenantSlug, currentUser)
        );
    }

    @PostMapping
    public ResponseEntity<CartItemResponse> addToCart(
            @PathVariable String tenantSlug,
            @Valid @RequestBody CartItemRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                cartService.addToCart(tenantSlug, request, currentUser)
        );
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<CartItemResponse> updateQuantity(
            @PathVariable String tenantSlug,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
                cartService.updateQuantity(tenantSlug, itemId, request, currentUser)
        );
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> removeItem(
            @PathVariable String tenantSlug,
            @PathVariable Long itemId,
            @AuthenticationPrincipal User currentUser) {
        cartService.removeItem(tenantSlug, itemId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @PathVariable String tenantSlug,
            @AuthenticationPrincipal User currentUser) {
        cartService.clearCart(tenantSlug, currentUser);
        return ResponseEntity.noContent().build();
    }
}
