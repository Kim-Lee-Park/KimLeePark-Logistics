package com.klp.payment.payment.domain.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Order 서비스에서 발행하는 주문 취소 이벤트 (구독용)
 */
public record OrderCancelledEvent(
    UUID orderId,
    String inventoryIdempotencyKey,
    String deliveryIdempotencyKey,
    List<ProductReplenishment> products,
    LocalDateTime occurredAt
) {

    public record ProductReplenishment(
        UUID productId,
        UUID hubId,
        Integer quantity
    ) {
    }
}
