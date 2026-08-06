package com.ecommerce.controller;

import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.dto.request.ProductSearchRequest;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.dto.request.StockUpdateRequest;
import com.ecommerce.model.User;
import com.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/{tenantSlug}/products")
public class MultiTenantProductController {

    private final ProductService productService;

    public MultiTenantProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @PathVariable String tenantSlug,
            @Valid ProductSearchRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(productService.getProducts(tenantSlug, request, currentUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable String tenantSlug,
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(productService.getProductById(tenantSlug, id, currentUser));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<ProductResponse> addProduct(
            @PathVariable String tenantSlug,
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.addProduct(tenantSlug, request, currentUser));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable String tenantSlug,
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(productService.updateProduct(tenantSlug, id, request, currentUser));
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<ProductResponse> updateStock(
            @PathVariable String tenantSlug,
            @PathVariable Long id,
            @Valid @RequestBody StockUpdateRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(productService.updateStock(tenantSlug, id, request, currentUser));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable String tenantSlug,
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        productService.deleteProduct(tenantSlug, id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
