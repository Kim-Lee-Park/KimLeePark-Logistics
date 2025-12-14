package com.klp.order.infrastructure.client.dto.inventory.request;

import java.util.List;
import java.util.UUID;

public record InventoryReservationRequest(
    UUID orderId,
    String idempotencyKey,
    List<ReservationItemRequest> items

) {

    public record ReservationItemRequest(
        UUID productId,
        UUID hubId,
        Integer quantity
    ) {

    }
}
