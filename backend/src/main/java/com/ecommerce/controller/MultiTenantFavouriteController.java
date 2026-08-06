package com.ecommerce.controller;

import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.model.User;
import com.ecommerce.service.FavouriteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/{tenantSlug}/favourites")
@PreAuthorize("hasAnyRole('USER', 'TENANT_ADMIN')")
public class MultiTenantFavouriteController {

    private final FavouriteService favouriteService;

    public MultiTenantFavouriteController(FavouriteService favouriteService) {
        this.favouriteService = favouriteService;
    }

    @PostMapping("/{productId}")
    public ResponseEntity<ProductResponse> addFavourite(
            @PathVariable String tenantSlug,
            @PathVariable Long productId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(favouriteService.addFavourite(tenantSlug, productId, currentUser));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeFavourite(
            @PathVariable String tenantSlug,
            @PathVariable Long productId,
            @AuthenticationPrincipal User currentUser) {
        favouriteService.removeFavourite(tenantSlug, productId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getUserFavourites(
            @PathVariable String tenantSlug,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(
                favouriteService.getUserFavourites(
                        tenantSlug,
                        currentUser
                )
        );
    }
}
