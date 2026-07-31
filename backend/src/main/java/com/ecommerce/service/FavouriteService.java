package com.ecommerce.service;

import com.ecommerce.dto.ProductResponse;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.FavouriteProduct;
import com.ecommerce.model.Product;
import com.ecommerce.model.Tenant;
import com.ecommerce.model.User;
import com.ecommerce.repository.FavouriteProductRepository;
import com.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavouriteService {

    private final FavouriteProductRepository favouriteProductRepository;
    private final ProductRepository productRepository;
    private final TenantService tenantService;
    private final ProductService productService;

    public FavouriteService(FavouriteProductRepository favouriteProductRepository, ProductRepository productRepository, TenantService tenantService, ProductService productService) {
        this.favouriteProductRepository = favouriteProductRepository;
        this.productRepository = productRepository;
        this.tenantService = tenantService;
        this.productService = productService;
    }

    @Transactional
    public ProductResponse addFavourite(String tenantSlug, Long productId, User currentUser) {
        Tenant tenant = tenantService.getTenantEntityBySlug(tenantSlug);
        Product product = productRepository.findByIdAndTenantId(productId, tenant.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId + " under brand " + tenantSlug));

        if (favouriteProductRepository.existsByUserIdAndProductId(currentUser.getId(), product.getId())) {
            throw new BadRequestException("Product is already in your favourites");
        }

        FavouriteProduct fav = FavouriteProduct.builder()
                .user(currentUser)
                .product(product)
                .build();

        favouriteProductRepository.save(fav);
        return productService.mapToResponse(product, currentUser);
    }

    @Transactional
    public void removeFavourite(String tenantSlug, Long productId, User currentUser) {
        Tenant tenant = tenantService.getTenantEntityBySlug(tenantSlug);
        Product product = productRepository.findByIdAndTenantId(productId, tenant.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId + " under brand " + tenantSlug));

        favouriteProductRepository.deleteByUserIdAndProductId(currentUser.getId(), product.getId());
    }

    public List<ProductResponse> getUserFavourites(User currentUser) {
        return favouriteProductRepository.findByUserId(currentUser.getId()).stream()
                .map(fav -> productService.mapToResponse(fav.getProduct(), currentUser))
                .collect(Collectors.toList());
    }
}
