package com.klp.delivery.delivery.domain.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record InventoryReplenishedEvent(
    UUID orderId,
    Long userId,
    String inventoryIdempotencyKey,
    String deliveryIdempotencyKey,
    String cancelReason,
    List<ProductReplenishment> products,
    LocalDateTime cancelledAt,
    LocalDateTime occurredAt
) {

    public record ProductReplenishment(
        UUID productId,
        UUID hubId,
        Integer quantity
    ) {

    }


}