package com.klp.hub.inventory.domain.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record InventoryReplenishedEvent(
    UUID orderId,
    List<ReplenishedItem> items,
    LocalDateTime occurredAt
) {

    public record ReplenishedItem(
        UUID productId,
        UUID hubId,
        Integer replenishedQuantity
    ) {

    }

    public static InventoryReplenishedEvent of(UUID orderId, List<ReplenishedItem> items) {
        return new InventoryReplenishedEvent(orderId, items, LocalDateTime.now());
    }
}