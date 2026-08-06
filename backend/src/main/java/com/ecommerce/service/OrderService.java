package com.ecommerce.service;

import com.ecommerce.dto.request.CreateOrderRequest;
import com.ecommerce.dto.request.OrderItemRequest;
import com.ecommerce.dto.response.OrderItemResponse;
import com.ecommerce.dto.response.OrderResponse;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.UnauthorizedAccessException;
import com.ecommerce.model.*;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final TenantService tenantService;
    private final ProductService productService;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, UserRepository userRepository, TenantService tenantService, ProductService productService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.tenantService = tenantService;
        this.productService = productService;
    }

    @Transactional
    public OrderResponse createOrder(String tenantSlug, CreateOrderRequest request, User currentUser) {

        log.info(
                "Creating order for user '{}' under tenant '{}'.",
                currentUser.getUsername(),
                tenantSlug
        );

        if (currentUser.getRole() == Role.ADMIN) {
            throw new UnauthorizedAccessException(
                    "Admin accounts are not allowed to place orders."
            );
        }

        validateOrderRequest(request);

        Tenant tenant = resolveOrderTenant(tenantSlug, request);

        User user = getPersistentUser(currentUser);

        int totalQuantity = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;

        Order order = initializeOrder(user, tenant);

        log.debug(
                "Processing {} order item(s).",
                request.getItems().size()
        );

        for (OrderItemRequest itemRequest : request.getItems()) {
            log.debug(
                    "Processing product '{}' with quantity {}.",
                    itemRequest.getProductId(),
                    itemRequest.getQuantity()
            );

            Product product = getTenantProduct(
                    tenant,
                    tenantSlug,
                    itemRequest.getProductId()
            );

            ensureProductActive(product);

            validateStock(product, itemRequest.getQuantity());

            reduceStock(product, itemRequest.getQuantity());

            BigDecimal subtotal = calculateSubtotal(
                    product,
                    itemRequest.getQuantity()
            );

            totalQuantity += itemRequest.getQuantity();
            totalAmount = totalAmount.add(subtotal);

            OrderItem orderItem = buildOrderItem(
                    order,
                    product,
                    itemRequest,
                    subtotal
            );

            order.getItems().add(orderItem);
        }

        order.setTotalQuantity(totalQuantity);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.COMPLETED);

        Order savedOrder = orderRepository.save(order);

        log.info(
                "Order '{}' created successfully for user '{}'.",
                savedOrder.getId(),
                currentUser.getUsername()
        );

        return mapToResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrderHistory(
            String tenantSlug,
            User currentUser
    ) {

        log.info(
                "Fetching order history for user '{}' under tenant '{}'.",
                currentUser.getUsername(),
                tenantSlug
        );

        if (tenantSlug == null
                || tenantSlug.isBlank()
                || tenantSlug.equalsIgnoreCase("global")) {

            return orderRepository
                    .findByUserIdOrderByOrderDateDesc(currentUser.getId())
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        }

        return orderRepository
                .findByUserIdAndTenantSlugOrderByOrderDateDesc(
                        currentUser.getId(),
                        tenantSlug
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getTenantOrders(String tenantSlug, User currentUser) {

        log.info(
                "Fetching all orders for tenant '{}'.",
                tenantSlug
        );


        Tenant tenant = tenantService.getTenantEntityBySlug(tenantSlug);

        productService.validateTenantAccess(currentUser, tenant);

        return orderRepository.findByTenantIdOrderByOrderDateDesc(tenant.getId()).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {

        log.info(
                "Fetching all orders across all tenants."
        );

        return orderRepository
                .findAllByOrderByOrderDateDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private OrderResponse mapToResponse(Order order) {

        log.debug(
                "Mapping order '{}' to response DTO.",
                order.getId()
        );


        List<OrderItemResponse> itemResponses = order.getItems()
                .stream()
                .map(this::mapOrderItem)
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .username(order.getUser().getUsername())
                .userFullName(order.getUser().getName())
                .tenantId(order.getTenant().getId())
                .tenantName(order.getTenant().getName())
                .tenantSlug(order.getTenant().getSlug())
                .orderDate(order.getOrderDate())
                .totalQuantity(order.getTotalQuantity())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .items(itemResponses)
                .build();
    }

    private void validateOrderRequest(CreateOrderRequest request) {

        if (request.getItems() == null || request.getItems().isEmpty()) {

            throw new BadRequestException(
                    "Order must contain at least one order item."
            );
        }
    }

    private Tenant resolveOrderTenant(
            String tenantSlug,
            CreateOrderRequest request
    ) {

        if (tenantSlug != null
                && !tenantSlug.isBlank()
                && !tenantSlug.equalsIgnoreCase("global")) {

            return tenantService.getTenantEntityBySlug(tenantSlug);
        }

        List<Product> products = request.getItems().stream()
                .map(OrderItemRequest::getProductId)
                .map(productRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        if (products.isEmpty()) {
            throw new ResourceNotFoundException(
                    "None of the requested products were found."
            );
        }

        List<Long> tenantIds = products.stream()
                .map(product -> product.getTenant().getId())
                .distinct()
                .toList();

        if (tenantIds.size() > 1) {
            throw new BadRequestException(
                    "Your cart contains products from multiple brands. "
                            + "Please place a separate order for each brand."
            );
        }

        return products.get(0).getTenant();
    }

    private User getPersistentUser(User currentUser) {

        return userRepository.findById(currentUser.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + currentUser.getId()
                        ));
    }

    private Order initializeOrder(
            User user,
            Tenant tenant
    ) {

        return Order.builder()
                .user(user)
                .tenant(tenant)
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .items(new ArrayList<>())
                .build();
    }

    private Product getTenantProduct(
            Tenant tenant,
            String tenantSlug,
            Long productId
    ) {

        return productRepository.findByIdAndTenantId(
                        productId,
                        tenant.getId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product with id "
                                        + productId
                                        + " not found under brand "
                                        + tenantSlug
                        ));
    }

    private void ensureProductActive(Product product) {
        if (!product.isActive()) {
            throw new ResourceNotFoundException(
                    "Product not found with id: " + product.getId()
            );
        }
    }

    private void validateStock(
            Product product,
            Integer requestedQuantity
    ) {

        if (requestedQuantity > product.getAvailableQuantity()) {

            log.warn(
                    "Insufficient stock for product '{}'. Requested={}, Available={}.",
                    product.getName(),
                    requestedQuantity,
                    product.getAvailableQuantity()
            );

            throw new InsufficientStockException(
                    "Cannot order "
                            + requestedQuantity
                            + " units of "
                            + product.getName()
                            + ". Available stock: "
                            + product.getAvailableQuantity()
            );
        }
    }

    private void reduceStock(
            Product product,
            Integer orderedQuantity
    ) {

        product.setAvailableQuantity(
                product.getAvailableQuantity() - orderedQuantity
        );

        productRepository.save(product);

        log.debug(
                "Reduced stock for product '{}'. Remaining quantity={}.",
                product.getName(),
                product.getAvailableQuantity()
        );
    }

    private BigDecimal calculateSubtotal(
            Product product,
            Integer quantity
    ) {

        return product.getPrice()
                .multiply(BigDecimal.valueOf(quantity));
    }

    private OrderItem buildOrderItem(
            Order order,
            Product product,
            OrderItemRequest itemRequest,
            BigDecimal subtotal
    ) {

        return OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(itemRequest.getQuantity())
                .unitPrice(product.getPrice())
                .subtotal(subtotal)
                .build();
    }

    private OrderItemResponse mapOrderItem(
            OrderItem item
    ) {

        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productCategory(item.getProduct().getCategory())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .productImageUrl(item.getProduct().getImageUrl())
                .build();
    }
}
