package com.klp.order.domain.repository;

import com.klp.order.domain.entity.idempotencykey.OrderOutboundRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderOutboundRequestRepository {

    OrderOutboundRequest save(OrderOutboundRequest orderOutboundRequest);

    Optional<OrderOutboundRequest> findById(UUID requestId);

    List<OrderOutboundRequest> findAll();

    Optional<OrderOutboundRequest> findByIdempotencyKey(String idempotencyKey);
}