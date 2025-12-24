package com.klp.hub.inventory.application.dto;

import java.util.List;
import java.util.UUID;

public record InventoryReservationCommand(
    UUID orderId,
    String idempotencyKey,
    List<ReservationItem> items
) {

    public record ReservationItem(
        UUID productId,
        UUID hubId,
        Integer quantity
    ) {

    }
}