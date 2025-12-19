package com.klp.hub.inventory.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record InventoryDbSyncEvent(
    UUID eventId,
    UUID orderId,
    UUID productId,
    UUID hubId,
    int quantity,
    SyncType syncType,
    LocalDateTime timestamp,
    String idempotencyKey
) {

    public static InventoryDbSyncEvent reserve(
        UUID orderId, UUID productId, UUID hubId, int quantity, String idempotencyKey
    ) {
        return new InventoryDbSyncEvent(
            UUID.randomUUID(),
            orderId,
            productId,
            hubId,
            quantity,
            SyncType.RESERVE,
            LocalDateTime.now(),
            idempotencyKey
        );
    }

    public static InventoryDbSyncEvent release(
        UUID orderId, UUID productId, UUID hubId, int quantity
    ) {
        return new InventoryDbSyncEvent(
            UUID.randomUUID(),
            orderId,
            productId,
            hubId,
            quantity,
            SyncType.RELEASE,
            LocalDateTime.now(),
            null
        );
    }
}
