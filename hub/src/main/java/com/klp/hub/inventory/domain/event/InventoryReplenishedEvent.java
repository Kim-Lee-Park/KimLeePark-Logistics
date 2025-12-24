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

    public static InventoryReplenishedEvent of(CouponCancelledEvent event) {
        List<ProductInfo> products = event.products().stream()
            .map(product -> new ProductInfo(
                product.productId(),
                product.hubId(),
                product.quantity()
            ))
            .toList();

        return new InventoryReplenishedEvent(
            event.paymentId(),
            event.orderId(),
            event.userId(),
            event.userCouponId(),
            event.inventoryIdempotencyKey(),
            event.deliveryIdempotencyKey(),
            event.reason(),
            products,
            event.cancelledAt(),
            LocalDateTime.now()
        );
    }
}