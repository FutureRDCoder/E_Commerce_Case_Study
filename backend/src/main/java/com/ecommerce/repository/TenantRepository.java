package com.ecommerce.repository;

import com.ecommerce.model.Tenant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {

    Optional<Tenant> findBySlugIgnoreCase(String slug);

    Optional<Tenant> findBySlugIgnoreCaseAndActiveTrue(String slug);

    Optional<Tenant> findByNameIgnoreCase(String name);

    boolean existsBySlugIgnoreCase(String slug);

    boolean existsByNameIgnoreCase(String name);

    Page<Tenant> findByActiveTrue(Pageable pageable);
}
