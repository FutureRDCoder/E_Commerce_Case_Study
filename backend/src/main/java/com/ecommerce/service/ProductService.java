package com.ecommerce.service;

import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.dto.request.ProductSearchRequest;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.dto.request.StockUpdateRequest;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.UnauthorizedAccessException;
import com.ecommerce.model.Product;
import com.ecommerce.model.Role;
import com.ecommerce.model.Tenant;
import com.ecommerce.model.User;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.FavouriteProductRepository;
import com.ecommerce.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.ecommerce.repository.OrderItemRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final TenantService tenantService;
    private final FavouriteProductRepository favouriteProductRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;

    public ProductService(ProductRepository productRepository, TenantService tenantService, FavouriteProductRepository favouriteProductRepository, OrderItemRepository orderItemRepository, CartItemRepository cartItemRepository) {
        this.productRepository = productRepository;
        this.tenantService = tenantService;
        this.favouriteProductRepository = favouriteProductRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartItemRepository = cartItemRepository;
    }

    public ProductResponse addProduct(String tenantSlug, ProductRequest request, User currentUser) {

        log.info(
                "Adding product '{}' under tenant '{}'.",
                request.getName(),
                tenantSlug
        );


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

        log.info(
                "Product '{}' created successfully.",
                saved.getId()
        );

        return mapToResponse(saved, currentUser);
    }

    public ProductResponse updateProduct(String tenantSlug, Long productId, ProductRequest request, User currentUser) {

        log.info(
                "Updating product '{}' under tenant '{}'.",
                productId,
                tenantSlug
        );


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

        log.info(
                "Product '{}' updated successfully.",
                updated.getId()
        );

        return mapToResponse(updated, currentUser);
    }

    public ProductResponse updateStock(String tenantSlug, Long productId, StockUpdateRequest request, User currentUser) {

        log.info(
                "Updating stock for product '{}'.",
                productId
        );

        Tenant tenant = tenantService.getTenantEntityBySlug(tenantSlug);
        validateTenantAccess(currentUser, tenant);

        Product product = productRepository.findByIdAndTenantId(productId, tenant.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId + " under tenant: " + tenantSlug));

        product.setAvailableQuantity(request.getAvailableQuantity());
        Product updated = productRepository.save(product);

        log.info(
                "Stock updated for product '{}'. New quantity={}.",
                updated.getId(),
                updated.getAvailableQuantity()
        );

        return mapToResponse(updated, currentUser);
    }

    @Transactional
    public void deleteProduct(String tenantSlug, Long productId, User currentUser) {

        log.info(
                "Deleting product '{}' under tenant '{}'.",
                productId,
                tenantSlug
        );

        Tenant tenant = tenantService.getTenantEntityBySlug(tenantSlug);
        validateTenantAccess(currentUser, tenant);

        Product product = productRepository.findByIdAndTenantId(productId, tenant.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId + " under tenant: " + tenantSlug));

        favouriteProductRepository.deleteByProductId(productId);
        orderItemRepository.deleteByProductId(productId);
        cartItemRepository.deleteByProductId(productId);
        productRepository.delete(product);

        log.info(
                "Product '{}' deleted successfully.",
                productId
        );
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(
            ProductSearchRequest request,
            User currentUser
    ) {
        log.info("Fetching all products for all tenants.");

        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by("id").descending()
        );

        String category = normalize(request.getCategory());
        String search = normalize(request.getSearch());
        BigDecimal minPrice = request.getMinPrice();
        BigDecimal maxPrice = request.getMaxPrice();

        validatePriceRange(minPrice, maxPrice);

        Page<Product> productPage = productRepository.searchProducts(
                null,
                category,
                search,
                minPrice,
                maxPrice,
                pageable
        );

        return productPage.map(product ->
                mapToResponse(product, currentUser)
        );
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(
            String tenantSlug,
            ProductSearchRequest request,
            User currentUser
    ) {

        log.info(
                "Fetching products for tenant '{}'.",
                tenantSlug
        );


        Tenant tenant = tenantService.getTenantEntityBySlug(tenantSlug);

        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by("id").descending()
        );

        String category = normalize(request.getCategory());
        String search = normalize(request.getSearch());
        BigDecimal minPrice = request.getMinPrice();
        BigDecimal maxPrice = request.getMaxPrice();

        validatePriceRange(minPrice, maxPrice);

        Page<Product> productPage = productRepository.searchProducts(
                tenant.getId(),
                category,
                search,
                minPrice,
                maxPrice,
                pageable
        );

        return productPage.map(product ->
                mapToResponse(product, currentUser)
        );
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(String tenantSlug, Long productId, User currentUser) {

        log.info(
                "Fetching product '{}' under tenant '{}'.",
                productId,
                tenantSlug
        );

        Tenant tenant = tenantService.getTenantEntityBySlug(tenantSlug);
        Product product = productRepository.findByIdAndTenantId(productId, tenant.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId + " under tenant: " + tenantSlug));
        return mapToResponse(product, currentUser);
    }

    void validateTenantAccess(
            User user,
            Tenant tenant
    ) {

        if (user.getRole() == Role.ADMIN) {
            return;
        }

        if (user.getRole() == Role.TENANT_ADMIN) {
            if (user.getTenant() != null
                    && user.getTenant().getId().equals(tenant.getId())) {
                return;
            }

            throw new UnauthorizedAccessException(
                    "Tenant user cannot perform management operations on another tenant's domain: "
                            + tenant.getSlug()
            );
        }

        throw new UnauthorizedAccessException(
                "User does not have permission to perform management operations."
        );
    }

    private String normalize(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private void validatePriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            log.warn(
                    "Invalid price range requested. minPrice={}, maxPrice={}.",
                    minPrice,
                    maxPrice
            );
            throw new BadRequestException(
                    "Minimum price cannot be greater than maximum price."
            );
        }
    }

    @Transactional(readOnly = true)
    public ProductResponse mapToResponse(Product product, User currentUser) {

        log.debug(
                "Mapping product '{}' to ProductResponse.",
                product.getId()
        );

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
