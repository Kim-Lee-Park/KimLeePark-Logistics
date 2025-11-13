package com.klp.order.order.domain.event;

import com.klp.order.common.event.DomainEvent;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
    UUID orderId,
    List<Product> products,
    LocalDateTime occurredAt
) implements DomainEvent {

    public OrderCreatedEvent {
        if (orderId == null) {
            throw new IllegalArgumentException("주문ID 는 필수값입니다.");
        }

        if (products == null || products.isEmpty()) {
            throw new IllegalArgumentException("상품은 필수값입니다.");
        }
    }

    @Override
    public String getEventId() {
        return orderId.toString();
    }

    @Override
    public LocalDateTime getOccurredAt() {
        return this.occurredAt;
    }

    public record Product(
        UUID productId,
        Integer quantity,
        Integer price
    ) {

    }
}
