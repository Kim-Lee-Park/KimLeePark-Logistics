package com.klp.order.infrastructure.event.event;

import com.klp.order.domain.entity.order.Order;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderCancelledEvent(
    UUID orderId,
    String InventoryIdempotencyKey,
    String DeliveryIdempotencyKey,
    List<ProductReplenishment> products,
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
            InventoryIdempotencyKey,
            DeliveryIdempotencyKey,
            products,
            LocalDateTime.now()
        );
    }
}