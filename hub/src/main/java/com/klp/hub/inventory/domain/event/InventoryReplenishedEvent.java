package com.klp.hub.inventory.domain.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record InventoryReplenishedEvent(
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

//    public static InventoryReplenishedEvent of(UUID orderId, List<ReplenishedItem> items) {
//        return new InventoryReplenishedEvent(orderId, items, LocalDateTime.now());
//    }

    public static InventoryReplenishedEvent of(
        UUID paymentId,
        UUID orderId,
        Long userId,
        UUID userCouponId,
        String inventoryIdempotencyKey,
        String deliveryIdempotencyKey,
        String reason,
        List<ProductInfo> products,
        LocalDateTime cancelledAt
    ) {
        return new InventoryReplenishedEvent(
            paymentId,
            orderId,
            userId,
            userCouponId,
            inventoryIdempotencyKey,
            deliveryIdempotencyKey,
            reason,
            products,
            cancelledAt,
            LocalDateTime.now()  // occurredAt은 현재 시각
        );
    }
}