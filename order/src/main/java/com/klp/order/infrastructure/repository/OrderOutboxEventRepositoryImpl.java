package com.klp.order.infrastructure.repository;

import com.klp.order.domain.entity.outbox.OrderOutboxEvent;
import com.klp.order.domain.repository.OrderOutboxEventRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderOutboxEventRepositoryImpl implements OrderOutboxEventRepository {

    private final OrderOutboxEventJpaRepository jpaRepository;

    @Override
    public OrderOutboxEvent save(OrderOutboxEvent event) {
        return jpaRepository.save(event);
    }

    @Override
    public List<OrderOutboxEvent> findPendingEvents() {
        return jpaRepository.findPendingEvents();
    }

    @Override
    public Optional<OrderOutboxEvent> findById(UUID eventId) {
        return jpaRepository.findById(eventId);
    }

    @Override
    public List<OrderOutboxEvent> findStuckPublishingEvents() {
        return jpaRepository.findStuckPublishingEvents();
    }

    @Override
    public List<OrderOutboxEvent> findFailedEvents() {
        return jpaRepository.findFailedEvents();
    }
}
