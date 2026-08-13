package com.ecommerce.repository;

import com.ecommerce.model.FavouriteProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavouriteProductRepository extends JpaRepository<FavouriteProduct, Long> {

    List<FavouriteProduct> findByUserIdAndProduct_Tenant_Slug(
            Long userId,
            String tenantSlug
    );

    List<FavouriteProduct> findByUserId(Long userId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    @Modifying
    @Transactional
    void deleteByUserIdAndProductId(Long userId, Long productId);
}
