package com.ecommerce.repository;

import com.ecommerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
