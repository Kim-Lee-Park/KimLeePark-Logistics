package com.klp.delivery.delivery.infrastructure.repository;

import com.klp.delivery.delivery.domain.entity.outbox.DeliveryOutboxEvent;
import com.klp.delivery.delivery.domain.repository.DeliveryOutboxEventRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DeliveryOutboxEventRepositoryImpl implements DeliveryOutboxEventRepository {

    private final DeliveryOutboxEventJpaRepository jpaRepository;

    @Override
    public DeliveryOutboxEvent save(DeliveryOutboxEvent event) {
        return jpaRepository.save(event);
    }

    @Override
    public List<DeliveryOutboxEvent> findPendingEvents() {
        return jpaRepository.findPendingEvents();
    }

    @Override
    public Optional<DeliveryOutboxEvent> findById(UUID eventId) {
        return jpaRepository.findById(eventId);
    }

    @Override
    public List<DeliveryOutboxEvent> findStuckPublishingEvents() {
        return jpaRepository.findStuckPublishingEvents();
    }

    @Override
    public List<DeliveryOutboxEvent> findFailedEvents() {
        return jpaRepository.findFailedEvents();
    }

    @Override
    public DeliveryOutboxEvent saveAndFlush(DeliveryOutboxEvent event) {
        return jpaRepository.saveAndFlush(event);
    }
}

