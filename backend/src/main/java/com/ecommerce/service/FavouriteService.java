package com.ecommerce.service;

import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.UnauthorizedAccessException;
import com.ecommerce.model.FavouriteProduct;
import com.ecommerce.model.Product;
import com.ecommerce.model.Role;
import com.ecommerce.model.Tenant;
import com.ecommerce.model.User;
import com.ecommerce.repository.FavouriteProductRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class FavouriteService {

    private final FavouriteProductRepository favouriteProductRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final TenantService tenantService;
    private final ProductService productService;

    public FavouriteService(FavouriteProductRepository favouriteProductRepository, ProductRepository productRepository, UserRepository userRepository, TenantService tenantService, ProductService productService) {
        this.favouriteProductRepository = favouriteProductRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.tenantService = tenantService;
        this.productService = productService;
    }

    @Transactional
    public ProductResponse addFavourite(
            String tenantSlug,
            Long productId,
            User currentUser
    ) {

        log.info("User '{}' is adding product '{}' to favourites for tenant '{}'.",
                currentUser.getUsername(), productId, tenantSlug);

        ensureNotAdmin(currentUser);

        Product product = getTenantProduct(tenantSlug, productId);

        if (!product.isActive()) {
            throw new ResourceNotFoundException(
                    "Product not found with id: " + productId
            );
        }

        if (favouriteProductRepository.existsByUserIdAndProductId(
                currentUser.getId(),
                product.getId())) {

            log.warn("Favourite already exists. User='{}', Product='{}'.",
                    currentUser.getUsername(), productId);

            throw new BadRequestException(
                    "Product is already in your favourites."
            );
        }

        User user = getPersistentUser(currentUser);

        FavouriteProduct favourite = FavouriteProduct.builder()
                .user(user)
                .product(product)
                .build();

        favouriteProductRepository.save(favourite);

        log.info("Product '{}' successfully added to favourites for user '{}'.",
                product.getName(), user.getUsername());

        return productService.mapToResponse(product, user);
    }

    @Transactional
    public void removeFavourite(
            String tenantSlug,
            Long productId,
            User currentUser
    ) {

        log.info("User '{}' is removing product '{}' from favourites for tenant '{}'.",
                currentUser.getUsername(), productId, tenantSlug);

        ensureNotAdmin(currentUser);

        favouriteProductRepository.deleteByUserIdAndProductId(
                currentUser.getId(),
                productId
        );

        log.info("Product '{}' removed from favourites for user '{}'.",
                productId, currentUser.getUsername());
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getUserFavourites(
            String tenantSlug,
            User currentUser
    ) {

        log.debug("Fetching favourites for user '{}' under tenant '{}'.",
                currentUser.getUsername(), tenantSlug);

        ensureNotAdmin(currentUser);

        List<FavouriteProduct> favourites;

        if (isGlobalSlug(tenantSlug)) {
            favourites = favouriteProductRepository.findByUserId(
                    currentUser.getId()
            );
        } else {
            favourites = favouriteProductRepository
                    .findByUserIdAndProduct_Tenant_Slug(
                            currentUser.getId(),
                            tenantSlug
                    );
        }

        List<ProductResponse> favouriteProducts = favourites
                .stream()
                .map(favourite ->
                        productService.mapToResponse(
                                favourite.getProduct(),
                                currentUser
                        ))
                .toList();

        log.debug("Found {} favourite products for user '{}'.",
                favouriteProducts.size(), currentUser.getUsername());

        return favouriteProducts;
    }

    private void ensureNotAdmin(User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) {
            throw new UnauthorizedAccessException(
                    "Admin accounts are not allowed to use favourites."
            );
        }
    }

    private Product getTenantProduct(String tenantSlug, Long productId) {

        log.debug("Looking up product '{}' for tenant '{}'.",
                productId, tenantSlug);

        Tenant tenant = tenantService.getTenantEntityBySlug(tenantSlug);

        return productRepository.findByIdAndTenantId(productId, tenant.getId())
                .orElseThrow(() -> {

                    log.warn("Product '{}' not found under tenant '{}'.",
                            productId, tenantSlug);

                    return new ResourceNotFoundException(
                            "Product not found with id: " + productId +
                                    " under brand " + tenantSlug);
                });
    }

    private User getPersistentUser(User currentUser) {

        log.debug("Loading persistent user '{}'.",
                currentUser.getUsername());

        return userRepository.findById(currentUser.getId())
                .orElseThrow(() -> {

                    log.warn("User with id '{}' not found.",
                            currentUser.getId());

                    return new ResourceNotFoundException(
                            "User not found with id: " + currentUser.getId());
                });
    }

    private boolean isGlobalSlug(String tenantSlug) {
        return tenantSlug == null
                || tenantSlug.isBlank()
                || tenantSlug.equalsIgnoreCase("global");
    }
}
