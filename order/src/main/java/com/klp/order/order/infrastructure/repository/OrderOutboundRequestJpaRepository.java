package com.klp.order.order.infrastructure.repository;

import com.klp.order.order.domain.entity.idempotencykey.OrderOutboundRequest;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderOutboundRequestJpaRepository extends
    JpaRepository<OrderOutboundRequest, UUID> {

    Optional<OrderOutboundRequest> findByIdempotencyKey(String idempotencyKey);
}