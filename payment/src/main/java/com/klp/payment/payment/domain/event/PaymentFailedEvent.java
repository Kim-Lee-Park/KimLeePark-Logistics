package com.klp.payment.payment.domain.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


public record PaymentFailedEvent(
    UUID paymentId,
    UUID orderId,
    Long userId,
    UUID userCouponId,
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
        UUID userCouponId,
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
            userCouponId,
            reason,
            products,
            LocalDateTime.now()
        );
    }

    public static PaymentFailedEvent from(
        UUID paymentId,
        UUID orderId,
        Long userId,
        UUID userCouponId,
        String reason,
        CouponUsedFailedEvent couponEvent
    ) {
        List<ProductInfo> products = couponEvent.products().stream()
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
            userCouponId,
            reason,
            products,
            LocalDateTime.now()
        );
    }
}
