package com.ecommerce.controller;

import com.ecommerce.dto.request.TenantRequest;
import com.ecommerce.dto.response.TenantResponse;
import com.ecommerce.service.TenantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/platform/tenants")
public class PlatformTenantController {

    private final TenantService tenantService;

    public PlatformTenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TenantResponse> createTenant(
            @Valid @RequestBody TenantRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(tenantService.createTenant(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTenant(
            @PathVariable Long id) {

        tenantService.deleteTenant(id);

        return ResponseEntity.noContent().build();
    }
}