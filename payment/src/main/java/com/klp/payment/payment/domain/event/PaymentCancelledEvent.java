package com.klp.payment.payment.domain.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 결제 취소 이벤트 (Inventory 보상 트랜잭션용)
 */
public record PaymentCancelledEvent(
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

    public static PaymentCancelledEvent from(
        UUID paymentId,
        UUID orderId,
        Long userId,
        String reason,
        List<ProductInfo> products
    ) {
        return new PaymentCancelledEvent(
            paymentId,
            orderId,
            userId,
            reason,
            products,
            LocalDateTime.now()
        );
    }
}
