package com.klp.order.infrastructure.repository;

import com.klp.order.domain.entity.saga.OrderSaga;
import com.klp.order.domain.repository.OrderSagaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderSagaRepositoryImpl implements OrderSagaRepository {

    private final OrderSagaJpaRepository orderSagaJpaRepository;

    @Override
    public OrderSaga save(OrderSaga saga) {
        return orderSagaJpaRepository.save(saga);
    }

    @Override
    public Optional<OrderSaga> findById(UUID sagaId) {
        return orderSagaJpaRepository.findById(sagaId);
    }

    @Override
    public Optional<OrderSaga> findByOrder_OrderId(UUID orderId) {
        return orderSagaJpaRepository.findByOrder_OrderId(orderId);
    }

    @Override
    public List<OrderSaga> findAll() {
        return orderSagaJpaRepository.findAll();
    }
}
