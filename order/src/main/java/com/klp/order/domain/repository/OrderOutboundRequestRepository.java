package com.klp.order.domain.repository;

import com.klp.order.domain.entity.idempotencykey.OrderOutboundRequest;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderOutboundRequestRepository extends JpaRepository<OrderOutboundRequest, UUID> {

    Optional<OrderOutboundRequest> findByIdempotencyKey(String idempotencyKey);
}
