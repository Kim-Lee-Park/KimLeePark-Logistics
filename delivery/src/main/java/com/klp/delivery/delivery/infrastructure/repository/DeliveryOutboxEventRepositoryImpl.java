package com.klp.delivery.delivery.infrastructure.repository;

import com.klp.delivery.delivery.domain.entity.outbox.DeliveryOutboxEvent;
import com.klp.delivery.delivery.domain.repository.DeliveryOutboxEventRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class DeliveryOutboxEventRepositoryImpl implements DeliveryOutboxEventRepository {

    private final DeliveryOutboxEventJpaRepository jpaRepository;

    @Override
    public DeliveryOutboxEvent save(DeliveryOutboxEvent event) {
        return jpaRepository.save(event);
    }

    @Override
    public List<DeliveryOutboxEvent> findPendingEvents(int limit) {
        return jpaRepository.findPendingEvents(limit);
    }

    @Override
    @Transactional
    public void markAsPublished(UUID id) {
        jpaRepository.markAsPublished(id);
    }

    @Override
    @Transactional
    public void markAsFailed(UUID id) {
        jpaRepository.markAsFailed(id);
    }
}

