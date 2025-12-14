package com.klp.order.infrastructure.event.event;

import com.klp.order.domain.entity.order.Order;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderCancelledEvent(
    UUID orderId,
    Long userId,
    UUID userCouponId,
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

    public static OrderCancelledEvent from(
        Order order,
        UUID userCouponId,
        String inventoryIdempotencyKey,
        String deliveryIdempotencyKey
    ) {
        List<ProductReplenishment> products = order.getOrderItems().stream()
            .map(item -> new ProductReplenishment(
                item.getProductId(),
                item.getHubId(),
                item.getQuantity()
            ))
            .toList();

        return new OrderCancelledEvent(
            order.getOrderId(),
            order.getUserId(),
            userCouponId,
            inventoryIdempotencyKey,
            deliveryIdempotencyKey,
            order.getCancellation().getCancelReason(),
            products,
            order.getCancellation().getCancelledAt(),
            LocalDateTime.now()
        );
    }
}