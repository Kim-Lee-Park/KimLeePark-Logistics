package com.klp.order.infrastructure.event;

import com.klp.order.domain.entity.order.Order;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderCancelledEvent(
    UUID orderId,
    String idempotencyKey,
    List<ProductReplenishment> products,
    LocalDateTime occurredAt
) {

    public record ProductReplenishment(
        UUID productId,
        UUID hubId,
        Integer quantity
    ) {

    }

    public static OrderCancelledEvent from(Order order, String idempotencyKey) {
        List<ProductReplenishment> products = order.getOrderItems().stream()
            .map(item -> new ProductReplenishment(
                item.getProductId(),
                item.getHubId(),
                item.getQuantity()
            ))
            .toList();

        return new OrderCancelledEvent(
            order.getOrderId(),
            idempotencyKey,
            products,
            LocalDateTime.now()
        );
    }
}