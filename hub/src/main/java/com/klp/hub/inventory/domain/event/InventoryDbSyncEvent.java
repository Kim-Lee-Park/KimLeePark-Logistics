package com.klp.hub.inventory.domain.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record InventoryDbSyncEvent(
    UUID eventId,
    UUID orderId,
    List<SyncItem> items,
    LocalDateTime timestamp,
    String idempotencyKey
) {

    public record SyncItem(
        UUID productId,
        UUID hubId,
        int quantity
    ) {

    }

    public static InventoryDbSyncEvent of(
        UUID orderId,
        List<SyncItem> items,
        String idempotencyKey
    ) {
        return new InventoryDbSyncEvent(
            UUID.randomUUID(),
            orderId,
            items,
            LocalDateTime.now(),
            idempotencyKey
        );
    }
}