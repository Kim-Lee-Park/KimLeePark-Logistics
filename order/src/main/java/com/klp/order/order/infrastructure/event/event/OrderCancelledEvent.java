package com.klp.order.order.infrastructure.event.event;

import com.klp.order.order.domain.entity.order.Order;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderCancelledEvent(
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

    public static OrderCancelledEvent from(Order order, String InventoryIdempotencyKey,
        String DeliveryIdempotencyKey) {
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
            InventoryIdempotencyKey,
            DeliveryIdempotencyKey,
            order.getCancellation().getCancelReason(),
            products,
            order.getCancellation().getCancelledAt(),
            LocalDateTime.now()
        );
    }
}