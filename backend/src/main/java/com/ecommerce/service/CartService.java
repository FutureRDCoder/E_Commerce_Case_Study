package com.ecommerce.service;

import com.ecommerce.dto.request.CartItemRequest;
import com.ecommerce.dto.request.UpdateCartItemRequest;
import com.ecommerce.dto.response.CartItemResponse;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.UnauthorizedAccessException;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Product;
import com.ecommerce.model.Role;
import com.ecommerce.model.Tenant;
import com.ecommerce.model.User;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final TenantService tenantService;

    public CartService(CartItemRepository cartItemRepository, ProductRepository productRepository, UserRepository userRepository, TenantService tenantService) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.tenantService = tenantService;
    }

    @Transactional
    public CartItemResponse addToCart(
            String tenantSlug,
            CartItemRequest request,
            User currentUser
    ) {

        log.info(
                "User '{}' is adding product '{}' (quantity {}) to cart for tenant '{}'.",
                currentUser.getUsername(),
                request.getProductId(),
                request.getQuantity(),
                tenantSlug
        );

        if (currentUser.getRole() == Role.ADMIN) {
            throw new UnauthorizedAccessException(
                    "Admin accounts are not allowed to add products to the cart."
            );
        }

        Tenant tenant = tenantService.getTenantEntityBySlug(tenantSlug);

        Product product = getTenantProduct(tenant, tenantSlug, request.getProductId());

        validateStock(product, request.getQuantity());

        User user = getPersistentUser(currentUser);

        CartItem cartItem = cartItemRepository
                .findByUserIdAndProductId(user.getId(), product.getId())
                .orElseGet(() ->
                        CartItem.builder()
                                .user(user)
                                .product(product)
                                .quantity(0)
                                .build()
                );

        int newQuantity = cartItem.getQuantity() + request.getQuantity();

        if (newQuantity > product.getAvailableQuantity()) {
            log.warn(
                    "Insufficient stock for product '{}'. Requested total={}, Available={}.",
                    product.getName(),
                    newQuantity,
                    product.getAvailableQuantity()
            );
            throw new InsufficientStockException(
                    "Cannot add " + request.getQuantity()
                            + " more units of " + product.getName()
                            + ". Available stock: "
                            + product.getAvailableQuantity()
            );
        }

        cartItem.setQuantity(newQuantity);

        CartItem saved = cartItemRepository.save(cartItem);

        log.info(
                "Product '{}' added to cart for user '{}'. Total quantity={}.",
                product.getName(),
                user.getUsername(),
                saved.getQuantity()
        );

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CartItemResponse> getCart(
            String tenantSlug,
            User currentUser
    ) {

        log.debug(
                "Fetching cart for user '{}' under tenant '{}'.",
                currentUser.getUsername(),
                tenantSlug
        );

        List<CartItemResponse> cart;

        if (isGlobalSlug(tenantSlug)) {
            cart = cartItemRepository
                    .findByUserId(currentUser.getId())
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        } else {
            cart = cartItemRepository
                    .findByUserIdAndProduct_Tenant_Slug(
                            currentUser.getId(),
                            tenantSlug
                    )
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        }

        log.debug(
                "Found {} item(s) in cart for user '{}'.",
                cart.size(),
                currentUser.getUsername()
        );

        return cart;
    }

    @Transactional
    public CartItemResponse updateQuantity(
            String tenantSlug,
            Long itemId,
            UpdateCartItemRequest request,
            User currentUser
    ) {

        log.info(
                "User '{}' is updating quantity to {} for cart item '{}' under tenant '{}'.",
                currentUser.getUsername(),
                request.getQuantity(),
                itemId,
                tenantSlug
        );

        CartItem cartItem = getOwnedCartItem(itemId, currentUser);

        Product product = getTenantProduct(
                cartItem.getProduct().getTenant(),
                tenantSlug,
                cartItem.getProduct().getId()
        );

        if (request.getQuantity() > product.getAvailableQuantity()) {
            log.warn(
                    "Insufficient stock for product '{}'. Requested={}, Available={}.",
                    product.getName(),
                    request.getQuantity(),
                    product.getAvailableQuantity()
            );
            throw new InsufficientStockException(
                    "Cannot set quantity to " + request.getQuantity()
                            + " for " + product.getName()
                            + ". Available stock: "
                            + product.getAvailableQuantity()
            );
        }

        cartItem.setQuantity(request.getQuantity());

        CartItem saved = cartItemRepository.save(cartItem);

        log.info(
                "Cart item '{}' updated to quantity {}.",
                saved.getId(),
                saved.getQuantity()
        );

        return mapToResponse(saved);
    }

    @Transactional
    public void removeItem(
            String tenantSlug,
            Long itemId,
            User currentUser
    ) {

        log.info(
                "User '{}' is removing cart item '{}' under tenant '{}'.",
                currentUser.getUsername(),
                itemId,
                tenantSlug
        );

        CartItem cartItem = getOwnedCartItem(itemId, currentUser);

        cartItemRepository.delete(cartItem);

        log.info(
                "Cart item '{}' removed.",
                itemId
        );
    }

    @Transactional
    public void clearCart(
            String tenantSlug,
            User currentUser
    ) {

        log.info(
                "Clearing cart for user '{}' under tenant '{}'.",
                currentUser.getUsername(),
                tenantSlug
        );

        if (isGlobalSlug(tenantSlug)) {
            cartItemRepository.deleteByUserId(currentUser.getId());
        } else {
            cartItemRepository.deleteByUserIdAndProduct_Tenant_Slug(
                    currentUser.getId(),
                    tenantSlug
            );
        }

        log.info(
                "Cart cleared for user '{}'.",
                currentUser.getUsername()
        );
    }

    private CartItem getOwnedCartItem(Long itemId, User currentUser) {
        return cartItemRepository.findByIdAndUserId(itemId, currentUser.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cart item not found with id: " + itemId
                        ));
    }

    private Product getTenantProduct(
            Tenant tenant,
            String tenantSlug,
            Long productId
    ) {
        return productRepository.findByIdAndTenantId(productId, tenant.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product with id " + productId
                                        + " not found under brand " + tenantSlug
                        ));
    }

    private void validateStock(Product product, Integer requestedQuantity) {
        if (requestedQuantity > product.getAvailableQuantity()) {
            log.warn(
                    "Insufficient stock for product '{}'. Requested={}, Available={}.",
                    product.getName(),
                    requestedQuantity,
                    product.getAvailableQuantity()
            );
            throw new InsufficientStockException(
                    "Cannot add " + requestedQuantity
                            + " units of " + product.getName()
                            + ". Available stock: "
                            + product.getAvailableQuantity()
            );
        }
    }

    private User getPersistentUser(User currentUser) {
        return userRepository.findById(currentUser.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + currentUser.getId()
                        ));
    }

    private boolean isGlobalSlug(String tenantSlug) {
        return tenantSlug == null
                || tenantSlug.isBlank()
                || tenantSlug.equalsIgnoreCase("global");
    }

    private CartItemResponse mapToResponse(CartItem item) {

        Product product = item.getProduct();

        BigDecimal subtotal = product.getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        return CartItemResponse.builder()
                .id(item.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productCategory(product.getCategory())
                .quantity(item.getQuantity())
                .unitPrice(product.getPrice())
                .subtotal(subtotal)
                .productImageUrl(product.getImageUrl())
                .availableQuantity(product.getAvailableQuantity())
                .tenantSlug(product.getTenant().getSlug())
                .build();
    }
}
