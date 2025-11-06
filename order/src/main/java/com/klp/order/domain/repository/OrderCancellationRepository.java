package com.klp.order.domain.repository;

import com.klp.order.domain.entity.cancel.OrderCancellation;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderCancellationRepository extends JpaRepository<OrderCancellation, UUID> {

    Optional<OrderCancellation> findByOrder_OrderId(UUID orderId);
}
