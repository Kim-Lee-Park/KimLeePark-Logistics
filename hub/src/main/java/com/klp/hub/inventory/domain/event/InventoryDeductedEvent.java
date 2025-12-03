package com.klp.hub.inventory.domain.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record InventoryDeductedEvent(
    UUID orderId,
    List<DeductedItem> items,
    LocalDateTime occurredAt
) {

    public record DeductedItem(
        UUID productId,
        UUID hubId,
        Integer deductedQuantity
    ) {

    }

    public static InventoryDeductedEvent of(UUID orderId, List<DeductedItem> items) {
        return new InventoryDeductedEvent(orderId, items, LocalDateTime.now());
    }
}
