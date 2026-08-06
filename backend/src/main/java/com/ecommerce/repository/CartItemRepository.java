package com.ecommerce.repository;

import com.ecommerce.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUserIdAndProduct_Tenant_Slug(
            Long userId,
            String tenantSlug
    );

    List<CartItem> findByUserId(Long userId);

    Optional<CartItem> findByUserIdAndProductId(
            Long userId,
            Long productId
    );

    Optional<CartItem> findByIdAndUserId(Long id, Long userId);

    @Modifying
    @Transactional
    void deleteByProductId(Long productId);

    @Modifying
    @Transactional
    void deleteByUserIdAndProduct_Tenant_Slug(
            Long userId,
            String tenantSlug
    );

    @Modifying
    @Transactional
    void deleteByUserId(Long userId);
}
