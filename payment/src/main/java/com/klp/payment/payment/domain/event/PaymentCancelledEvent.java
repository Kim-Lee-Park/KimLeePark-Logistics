package com.klp.payment.payment.domain.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


public record PaymentCancelledEvent(
    UUID paymentId,
    UUID orderId,
    Long userId,
    String reason,
    List<ProductInfo> products,
    LocalDateTime cancelledAt,
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
        String canelReason,
        List<ProductInfo> products,
        LocalDateTime cancelledAt
    ) {
        return new PaymentCancelledEvent(
            paymentId,
            orderId,
            userId,
            canelReason,
            products,
            cancelledAt,
            java.time.LocalDateTime.now()
        );
    }
}
