package com.ecommerce.repository;

import com.ecommerce.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);

    List<Order> findByUserIdAndTenantSlugOrderByOrderDateDesc(
            Long userId,
            String tenantSlug
    );

    List<Order> findByTenantIdOrderByOrderDateDesc(Long tenantId);

    List<Order> findAllByOrderByOrderDateDesc();
}
