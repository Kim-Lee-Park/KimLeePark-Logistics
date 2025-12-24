package com.klp.order.infrastructure.repository;

import com.klp.order.domain.entity.cancel.OrderCancellation;
import com.klp.order.domain.repository.OrderCancellationRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderCancellationRepositoryImpl implements OrderCancellationRepository {

    private final OrderCancellationJpaRepository orderCancellationJpaRepository;

    @Override
    public OrderCancellation save(OrderCancellation orderCancellation) {
        return orderCancellationJpaRepository.save(orderCancellation);
    }

    @Override
    public Optional<OrderCancellation> findById(UUID orderCancellationId) {
        return orderCancellationJpaRepository.findById(orderCancellationId);
    }

    @Override
    public List<OrderCancellation> findAll() {
        return orderCancellationJpaRepository.findAll();
    }

    @Override
    public void deleteById(UUID orderCancellationId) {
        orderCancellationJpaRepository.deleteById(orderCancellationId);
    }

    @Override
    public Optional<OrderCancellation> findByOrderId(UUID orderId) {
        return orderCancellationJpaRepository.findByOrderId(orderId);
    }
}