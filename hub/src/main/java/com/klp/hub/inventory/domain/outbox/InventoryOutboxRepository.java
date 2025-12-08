package com.klp.hub.inventory.domain.outbox;

import java.util.List;
import java.util.UUID;

public interface InventoryOutboxRepository {

    InventoryOutbox save(InventoryOutbox outbox);

    List<InventoryOutbox> findPendingEvents(int limit);

    void markAsPublished(UUID outboxId);

    void markAsFailed(UUID outboxId);

    void deletePublishedEvents();
}
