package com.klp.order.infrastructure.event;

import com.klp.order.domain.entity.order.Order;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
    UUID orderId,
    String idempotencyKey,
    List<ProductDeduction> products,
    LocalDateTime occurredAt
) {

    public record ProductDeduction(
        UUID productId,
        UUID hubId,
        Integer quantity
    ) {

    }

    public static OrderCreatedEvent from(Order order, String idempotencyKey) {
        List<ProductDeduction> products = order.getOrderItems().stream()
            .map(item -> new ProductDeduction(
                item.getProductId(),
                item.getHubId(),
                item.getQuantity()
            ))
            .toList();

        return new OrderCreatedEvent(
            order.getOrderId(),
            idempotencyKey,
            products,
            LocalDateTime.now()
        );
    }
}