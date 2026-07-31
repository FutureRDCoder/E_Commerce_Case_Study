package com.ecommerce.service;

import com.ecommerce.dto.TenantRequest;
import com.ecommerce.dto.TenantResponse;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Tenant;
import com.ecommerce.model.Order;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.repository.FavouriteProductRepository;
import com.ecommerce.repository.OrderItemRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.TenantRepository;
import com.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final FavouriteProductRepository favouriteProductRepository;
    private final OrderItemRepository orderItemRepository;

    public TenantService(
            TenantRepository tenantRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            OrderRepository orderRepository,
            FavouriteProductRepository favouriteProductRepository,
            OrderItemRepository orderItemRepository) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.favouriteProductRepository = favouriteProductRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public TenantResponse createTenant(TenantRequest request) {
        String slug = request.getSlug().toLowerCase().trim();
        if (tenantRepository.existsBySlugIgnoreCase(slug)) {
            throw new BadRequestException("Tenant slug already exists: " + slug);
        }
        if (tenantRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BadRequestException("Tenant name already exists: " + request.getName());
        }

        Tenant tenant = Tenant.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .logoUrl(request.getLogoUrl())
                .build();

        Tenant saved = tenantRepository.save(tenant);
        return mapToResponse(saved);
    }

    public List<TenantResponse> getAllTenants() {
        return tenantRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TenantResponse getTenantBySlug(String slug) {
        Tenant tenant = tenantRepository.findBySlugIgnoreCase(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with slug: " + slug));
        return mapToResponse(tenant);
    }

    @Transactional
    public void deleteTenant(Long id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));

        // 1. Dissociate tenant from associated users
        List<User> users = userRepository.findByTenantId(id);
        for (User user : users) {
            user.setTenant(null);
            userRepository.save(user);
        }

        // 2. Delete tenant orders
        List<Order> orders = orderRepository.findByTenantIdOrderByOrderDateDesc(id);
        orderRepository.deleteAll(orders);

        // 3. Delete tenant products and their favourite / order item references
        List<Product> products = productRepository.findByTenantId(id);
        for (Product product : products) {
            favouriteProductRepository.deleteByProductId(product.getId());
            orderItemRepository.deleteByProductId(product.getId());
        }
        productRepository.deleteAll(products);

        // 4. Delete tenant entity
        tenantRepository.delete(tenant);
    }

    public Tenant getTenantEntityBySlug(String slug) {
        return tenantRepository.findBySlugIgnoreCase(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant brand not found: " + slug));
    }

    private TenantResponse mapToResponse(Tenant tenant) {
        return TenantResponse.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .slug(tenant.getSlug())
                .description(tenant.getDescription())
                .logoUrl(tenant.getLogoUrl())
                .build();
    }
}
