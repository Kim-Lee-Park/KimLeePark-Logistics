package com.klp.promotion.coupon.domain.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CouponRestoredEvent(
    UUID paymentId,
    UUID orderId,
    Long userId,
    UUID userCouponId,
    String inventoryIdempotencyKey,
    String deliveryIdempotencyKey,
    String reason,
    List<CancelledItemDto> products,
    LocalDateTime cancelledAt,
    LocalDateTime occurredAt
) {

    public record CancelledItemDto(
        UUID productId,
        UUID hubId,
        Integer quantity
    ) {

    }

    public static CouponRestoredEvent from(PaymentCancelledEvent event) {
        List<CancelledItemDto> items = event.products().stream()
            .map(product -> new CancelledItemDto(
                product.productId(),
                product.hubId(),
                product.quantity()
            ))
            .toList();

        return new CouponRestoredEvent(
            event.paymentId(),
            event.orderId(),
            event.userId(),
            event.userCouponId(),
            event.inventoryIdempotencyKey(),
            event.deliveryIdempotencyKey(),
            event.reason(),
            items,
            event.cancelledAt(),
            event.occurredAt()
        );
    }
}