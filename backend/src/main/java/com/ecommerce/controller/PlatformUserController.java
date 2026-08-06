package com.ecommerce.controller;

import com.ecommerce.dto.request.AssignTenantRequest;
import com.ecommerce.dto.response.UserResponse;
import com.ecommerce.model.Role;
import com.ecommerce.service.PlatformUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/platform/users")
public class PlatformUserController {

    private final PlatformUserService platformUserService;

    public PlatformUserController(PlatformUserService platformUserService) {
        this.platformUserService = platformUserService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponse>> getUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page number cannot be negative.") int page,
            @RequestParam(defaultValue = "100")
            @Min(value = 1, message = "Page size must be at least 1.")
            @Max(value = 100, message = "Page size cannot exceed 100.") int size) {

        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(
                platformUserService.getUsers(role, pageable)
        );
    }

    @PutMapping("/{userId}/tenant")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> assignTenant(
            @PathVariable Long userId,
            @Valid @RequestBody AssignTenantRequest request) {

        return ResponseEntity.ok(
                platformUserService.assignTenant(userId, request)
        );
    }
}
