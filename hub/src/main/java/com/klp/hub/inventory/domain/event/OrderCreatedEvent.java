package com.klp.hub.inventory.domain.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
    UUID orderId,
    String idempotencyKey,
    List<OrderItemDto> items,
    LocalDateTime occurredAt
) {

    public record OrderItemDto(
        UUID productId,
        UUID hubId,
        Integer quantity
    ) {

    }

    public static OrderCreatedEvent create(UUID orderId, String idempotencyKey, List<OrderItemDto> items) {
        return new OrderCreatedEvent(orderId, idempotencyKey, items, LocalDateTime.now());
    }
}
