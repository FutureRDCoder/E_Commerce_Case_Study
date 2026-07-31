package com.ecommerce.repository;

import com.ecommerce.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);

    Page<Order> findByUserIdOrderByOrderDateDesc(Long userId, Pageable pageable);

    List<Order> findByTenantIdOrderByOrderDateDesc(Long tenantId);

    Page<Order> findByTenantIdOrderByOrderDateDesc(Long tenantId, Pageable pageable);

    List<Order> findByUserIdAndTenantIdOrderByOrderDateDesc(Long userId, Long tenantId);
}
