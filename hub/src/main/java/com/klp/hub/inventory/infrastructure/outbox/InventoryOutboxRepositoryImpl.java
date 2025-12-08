package com.klp.hub.inventory.infrastructure.outbox;

import com.klp.hub.inventory.domain.outbox.InventoryOutbox;
import com.klp.hub.inventory.domain.outbox.InventoryOutboxRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InventoryOutboxRepositoryImpl implements InventoryOutboxRepository {

    private final InventoryOutboxJpaRepository jpaRepository;

    @Override
    public InventoryOutbox save(InventoryOutbox outbox) {
        return jpaRepository.save(outbox);
    }

    @Override
    public List<InventoryOutbox> findPendingEvents(int limit) {
        return jpaRepository.findPendingEvents(limit);
    }

    @Override
    public void markAsPublished(UUID outboxId) {
        jpaRepository.markAsPublished(outboxId);
    }

    @Override
    public void markAsFailed(UUID outboxId) {
        jpaRepository.markAsFailed(outboxId);
    }

    @Override
    public void deletePublishedEvents() {
        jpaRepository.deletePublishedEvents();
    }
}
