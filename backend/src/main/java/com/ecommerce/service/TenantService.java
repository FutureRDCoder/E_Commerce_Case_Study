package com.ecommerce.service;

import com.ecommerce.dto.request.TenantRequest;
import com.ecommerce.dto.response.TenantResponse;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Tenant;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.TenantRepository;
import com.ecommerce.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    public TenantService(
            TenantRepository tenantRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            CartItemRepository cartItemRepository) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
    }

    public TenantResponse createTenant(TenantRequest request) {

        log.info(
                "Creating tenant '{}'.",
                request.getName()
        );

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

        log.info(
                "Tenant '{}' created successfully.",
                saved.getId()
        );

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<TenantResponse> getAllTenants(Pageable pageable) {

        log.info("Fetching all active tenants.");

        return tenantRepository
                .findByActiveTrue(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public TenantResponse getTenantBySlug(String slug) {

        log.info(
                "Fetching tenant '{}'.",
                slug
        );

        Tenant tenant = tenantRepository.findBySlugIgnoreCaseAndActiveTrue(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with slug: " + slug));
        return mapToResponse(tenant);
    }

    @Transactional
    public void deleteTenant(Long id) {

        log.info(
                "Deleting tenant '{}'.",
                id
        );

        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));

        // 1. Dissociate tenant from associated users
        List<User> users = userRepository.findByTenantId(id);
        for (User user : users) {
            user.setTenant(null);
            userRepository.save(user);
        }

        // 2. Deactivate the tenant's products (orders and favourites are preserved forever)
        List<Product> products = productRepository.findByTenantId(id);
        for (Product product : products) {
            cartItemRepository.deleteByProductId(product.getId());
            product.setActive(false);
            productRepository.save(product);
        }

        // 3. Deactivate the tenant instead of deleting it so historical
        //    orders and favourites remain visible forever
        tenant.setActive(false);
        tenantRepository.save(tenant);

        log.info(
                "Tenant '{}' deactivated successfully.",
                id
        );
    }

    @Transactional(readOnly = true)
    public Tenant getTenantEntityBySlug(String slug) {
        return tenantRepository.findBySlugIgnoreCaseAndActiveTrue(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant brand not found: " + slug));
    }

    @Transactional(readOnly = true)
    private TenantResponse mapToResponse(Tenant tenant) {

        log.debug(
                "Mapping tenant '{}' to response.",
                tenant.getId()
        );

        return TenantResponse.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .slug(tenant.getSlug())
                .description(tenant.getDescription())
                .logoUrl(tenant.getLogoUrl())
                .build();
    }
}
