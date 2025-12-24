package com.klp.order.infrastructure.repository;

import com.klp.order.domain.entity.cancel.OrderCancellation;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderCancellationJpaRepository extends JpaRepository<OrderCancellation, UUID> {

    Optional<OrderCancellation> findByOrderId(UUID orderId);
}