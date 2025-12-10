package com.klp.order.order.infrastructure.repository;

import com.klp.order.order.domain.entity.idempotencykey.OrderOutboundRequest;
import com.klp.order.order.domain.repository.OrderOutboundRequestRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderOutboundRequestRepositoryImpl implements OrderOutboundRequestRepository {

    private final OrderOutboundRequestJpaRepository orderOutboundRequestJpaRepository;

    @Override
    public OrderOutboundRequest save(OrderOutboundRequest orderOutboundRequest) {
        return orderOutboundRequestJpaRepository.save(orderOutboundRequest);
    }

    @Override
    public Optional<OrderOutboundRequest> findById(UUID requestId) {
        return orderOutboundRequestJpaRepository.findById(requestId);
    }

    @Override
    public List<OrderOutboundRequest> findAll() {
        return orderOutboundRequestJpaRepository.findAll();
    }

    @Override
    public Optional<OrderOutboundRequest> findByIdempotencyKey(String idempotencyKey) {
        return orderOutboundRequestJpaRepository.findByIdempotencyKey(idempotencyKey);
    }
}