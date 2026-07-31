package com.ecommerce.service;

import com.ecommerce.dto.ProductRequest;
import com.ecommerce.dto.ProductResponse;
import com.ecommerce.dto.StockUpdateRequest;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.UnauthorizedAccessException;
import com.ecommerce.model.Product;
import com.ecommerce.model.Role;
import com.ecommerce.model.Tenant;
import com.ecommerce.model.User;
import com.ecommerce.repository.FavouriteProductRepository;
import com.ecommerce.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.ecommerce.repository.OrderItemRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final TenantService tenantService;
    private final FavouriteProductRepository favouriteProductRepository;
    private final OrderItemRepository orderItemRepository;

    public ProductService(ProductRepository productRepository, TenantService tenantService, FavouriteProductRepository favouriteProductRepository, OrderItemRepository orderItemRepository) {
        this.productRepository = productRepository;
        this.tenantService = tenantService;
        this.favouriteProductRepository = favouriteProductRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public ProductResponse addProduct(String tenantSlug, ProductRequest request, User currentUser) {
        Tenant tenant = tenantService.getTenantEntityBySlug(tenantSlug);
        validateTenantAccess(currentUser, tenant);

        Product product = Product.builder()
                .tenant(tenant)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .availableQuantity(request.getAvailableQuantity())
                .imageUrl(request.getImageUrl())
                .build();

        Product saved = productRepository.save(product);
        return mapToResponse(saved, currentUser);
    }

    public ProductResponse updateProduct(String tenantSlug, Long productId, ProductRequest request, User currentUser) {
        Tenant tenant = tenantService.getTenantEntityBySlug(tenantSlug);
        validateTenantAccess(currentUser, tenant);

        Product product = productRepository.findByIdAndTenantId(productId, tenant.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId + " under tenant: " + tenantSlug));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());
        product.setAvailableQuantity(request.getAvailableQuantity());
        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }

        Product updated = productRepository.save(product);
        return mapToResponse(updated, currentUser);
    }

    public ProductResponse updateStock(String tenantSlug, Long productId, StockUpdateRequest request, User currentUser) {
        Tenant tenant = tenantService.getTenantEntityBySlug(tenantSlug);
        validateTenantAccess(currentUser, tenant);

        Product product = productRepository.findByIdAndTenantId(productId, tenant.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId + " under tenant: " + tenantSlug));

        product.setAvailableQuantity(request.getAvailableQuantity());
        Product updated = productRepository.save(product);
        return mapToResponse(updated, currentUser);
    }

    @Transactional
    public void deleteProduct(String tenantSlug, Long productId, User currentUser) {
        Tenant tenant = tenantService.getTenantEntityBySlug(tenantSlug);
        validateTenantAccess(currentUser, tenant);

        Product product = productRepository.findByIdAndTenantId(productId, tenant.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId + " under tenant: " + tenantSlug));

        favouriteProductRepository.deleteByProductId(productId);
        orderItemRepository.deleteByProductId(productId);
        productRepository.delete(product);
    }

    public Page<ProductResponse> getProducts(String tenantSlug, String category, String search, int page, int size, User currentUser) {
        Tenant tenant = tenantService.getTenantEntityBySlug(tenantSlug);
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<Product> productPage;
        if (category != null && !category.isBlank() && search != null && !search.isBlank()) {
            productPage = productRepository.findByTenantIdAndCategoryIgnoreCaseAndNameContainingIgnoreCase(
                    tenant.getId(), category.trim(), search.trim(), pageable);
        } else if (category != null && !category.isBlank()) {
            productPage = productRepository.findByTenantIdAndCategoryIgnoreCase(
                    tenant.getId(), category.trim(), pageable);
        } else if (search != null && !search.isBlank()) {
            productPage = productRepository.findByTenantIdAndNameContainingIgnoreCase(
                    tenant.getId(), search.trim(), pageable);
        } else {
            productPage = productRepository.findByTenantId(tenant.getId(), pageable);
        }

        return productPage.map(p -> mapToResponse(p, currentUser));
    }

    public ProductResponse getProductById(String tenantSlug, Long productId, User currentUser) {
        Tenant tenant = tenantService.getTenantEntityBySlug(tenantSlug);
        Product product = productRepository.findByIdAndTenantId(productId, tenant.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId + " under tenant: " + tenantSlug));
        return mapToResponse(product, currentUser);
    }

    public void validateTenantAccess(User user, Tenant tenant) {
        if (user.getRole() == Role.ADMIN) {
            return;
        }
        if (user.getRole() == Role.TENANT_ADMIN) {
            if (user.getTenant() == null || !user.getTenant().getId().equals(tenant.getId())) {
                throw new UnauthorizedAccessException("Tenant user cannot perform management operations on another tenant's domain: " + tenant.getSlug());
            }
        }
    }

    public ProductResponse mapToResponse(Product product, User currentUser) {
        boolean isFav = false;
        if (currentUser != null) {
            isFav = favouriteProductRepository.existsByUserIdAndProductId(currentUser.getId(), product.getId());
        }
        return ProductResponse.builder()
                .id(product.getId())
                .tenantId(product.getTenant().getId())
                .tenantName(product.getTenant().getName())
                .tenantSlug(product.getTenant().getSlug())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .availableQuantity(product.getAvailableQuantity())
                .imageUrl(product.getImageUrl())
                .isFavourite(isFav)
                .build();
    }
}
