package com.ecommerce.controller;

import com.ecommerce.dto.response.TenantResponse;
import com.ecommerce.service.TenantService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/tenants")
public class PublicTenantController {

    private final TenantService tenantService;

    public PublicTenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping
    public ResponseEntity<Page<TenantResponse>> getAllTenants(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page number cannot be negative.") int page,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Page size must be at least 1.")
            @Max(value = 100, message = "Page size cannot exceed 100.") int size) {

        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(
                tenantService.getAllTenants(pageable)
        );
    }

    @GetMapping("/{slug}")
    public ResponseEntity<TenantResponse> getTenantBySlug(
            @PathVariable String slug) {

        return ResponseEntity.ok(
                tenantService.getTenantBySlug(slug)
        );
    }
}