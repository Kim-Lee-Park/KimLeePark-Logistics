package com.klp.order.domain.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
    UUID orderId,
    List<Product> products,
    LocalDateTime occurredAt
) {

    public OrderCreatedEvent {
        if (orderId == null) {
            throw new IllegalArgumentException("주문ID 는 필수값입니다.");
        }

        if (products == null || products.isEmpty()) {
            throw new IllegalArgumentException("상품은 필수값입니다.");
        }
    }

    public record Product(
        UUID productId,
        Integer quantity,
        Integer price
    ) {

    }
}
