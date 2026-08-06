package com.ecommerce.repository;

import com.ecommerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByTenantId(Long tenantId);

    Page<Product> findByTenantId(Long tenantId, Pageable pageable);

    Page<Product> findByTenantIdAndCategoryIgnoreCase(Long tenantId, String category, Pageable pageable);

    Page<Product> findByTenantIdAndNameContainingIgnoreCase(Long tenantId, String name, Pageable pageable);

    Page<Product> findByTenantIdAndCategoryIgnoreCaseAndNameContainingIgnoreCase(Long tenantId, String category, String name, Pageable pageable);

    Optional<Product> findByIdAndTenantId(Long id, Long tenantId);
    
    Page<Product> findByCategoryIgnoreCase(String category, Pageable pageable);
    
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    Page<Product> findByCategoryIgnoreCaseAndNameContainingIgnoreCase(String category, String name, Pageable pageable);

    @Query("""
            SELECT p FROM Product p
            WHERE p.active = TRUE
              AND (:tenantId IS NULL OR p.tenant.id = :tenantId)
              AND (:category IS NULL OR LOWER(p.category) = LOWER(:category))
              AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
              AND (:minPrice IS NULL OR p.price >= :minPrice)
              AND (:maxPrice IS NULL OR p.price <= :maxPrice)
            """)
    Page<Product> searchProducts(
            @Param("tenantId") Long tenantId,
            @Param("category") String category,
            @Param("name") String name,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);
}
