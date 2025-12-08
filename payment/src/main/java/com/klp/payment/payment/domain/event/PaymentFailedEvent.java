package com.klp.payment.payment.domain.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 결제 실패 이벤트 (발행용 → Inventory 재고 복구)
 */
public record PaymentFailedEvent(
    UUID paymentId,
    UUID orderId,
    Long userId,
    String reason,
    List<ProductInfo> products,
    LocalDateTime occurredAt
) {

    public record ProductInfo(
        UUID productId,
        UUID hubId,
        Integer quantity
    ) {

    }

    public static PaymentFailedEvent from(
        UUID paymentId,
        UUID orderId,
        Long userId,
        String reason,
        OrderCreatedEvent orderEvent
    ) {
        List<ProductInfo> products = orderEvent.products().stream()
            .map(p -> new ProductInfo(
                p.productId(),
                p.hubId(),
                p.quantity()
            ))
            .toList();

        return new PaymentFailedEvent(
            paymentId,
            orderId,
            userId,
            reason,
            products,
            LocalDateTime.now()
        );
    }
}
