package com.klp.order.infrastructure.repository;

import com.klp.order.domain.entity.order.Order;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<Order, UUID> {

    List<Order> findByDeletedAtIsNull();

    List<Order> findBySupplierId(Long supplierId);

    List<Order> findByCustomerId(Long customerId);

    Optional<Order> findByOrderIdAndDeletedAtIsNull(UUID orderId);
}