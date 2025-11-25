package com.klp.order.application.service;

import com.klp.order.domain.entity.order.Order;
import com.klp.order.domain.entity.saga.OrderSaga;
import com.klp.order.domain.repository.OrderSagaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderSagaService {

    private final OrderSagaRepository orderSagaRepository;

    @Transactional
    public OrderSaga createSaga(Order order) {
        return OrderSaga.create(order);
    }

    @Transactional
    public OrderSaga save(OrderSaga saga) {
        return orderSagaRepository.save(saga);
    }

    @Transactional(readOnly = true)
    public Optional<OrderSaga> findById(UUID sagaId) {
        return orderSagaRepository.findById(sagaId);
    }

    @Transactional(readOnly = true)
    public Optional<OrderSaga> findByOrderId(UUID orderId) {
        return orderSagaRepository.findByOrder_OrderId(orderId);
    }

    @Transactional(readOnly = true)
    public List<OrderSaga> findAll() {
        return orderSagaRepository.findAll();
    }
}
