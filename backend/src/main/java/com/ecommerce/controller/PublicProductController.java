package com.ecommerce.controller;

import com.ecommerce.dto.request.ProductSearchRequest;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.model.User;
import com.ecommerce.service.AuthService;
import com.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/products")
public class PublicProductController {

    private final ProductService productService;
    private final AuthService authService;

    public PublicProductController(ProductService productService, AuthService authService) {
        this.productService = productService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @Valid ProductSearchRequest request) {

        User currentUser = authService.getCurrentUser().orElse(null);

        return ResponseEntity.ok(
                productService.getAllProducts(request, currentUser)
        );
    }
}
