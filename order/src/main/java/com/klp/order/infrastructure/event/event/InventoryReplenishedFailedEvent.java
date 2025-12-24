package com.klp.order.infrastructure.event.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record InventoryReplenishedFailedEvent(
    UUID paymentId,
    UUID orderId,
    Long userId,
    UUID userCouponId,
    String inventoryIdempotencyKey,
    String deliveryIdempotencyKey,
    String reason,
    List<ProductInfo> products,
    LocalDateTime cancelledAt,
    LocalDateTime occurredAt
) {

    public record ProductInfo(
        UUID productId,
        UUID hubId,
        Integer quantity
    ) {

    }
}
