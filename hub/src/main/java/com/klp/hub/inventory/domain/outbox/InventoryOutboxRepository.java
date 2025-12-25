package com.klp.hub.inventory.domain.outbox;

import java.util.List;
import java.util.UUID;

public interface InventoryOutboxRepository {

    InventoryOutbox save(InventoryOutbox outbox);

    List<InventoryOutbox> findPendingEvents(int limit);

    List<InventoryOutbox> findPendingDbSyncEvents();

    void markAsPublished(UUID outboxId);

    void markAsPublishedBatch(List<UUID> outboxIds);

    void markAsFailed(UUID outboxId);

    void markAsFailedBatch(List<UUID> outboxIds);

    void deletePublishedEvents();

    void saveAllInBatch(List<InventoryOutbox> outboxes);
}
