package com.klp.promotion.coupon.domain.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CouponUseFailedEvent(
    UUID orderId,
    UUID userCouponId,
    UUID paymentId,
    Long userId,
    List<ProductInfo> products,
    LocalDateTime occurredAt
) {

    public record ProductInfo(
        UUID productId,
        UUID hubId,
        Integer quantity
    ) {

    }

    public static CouponUseFailedEvent from(
        PaymentApprovedEvent event
    ) {
        List<ProductInfo> products = event.products().stream()
            .map(p -> new ProductInfo(
                p.productId(),
                p.hubId(),
                p.quantity()
            ))
            .toList();

        return new CouponUseFailedEvent(
            event.orderId(),
            event.userCouponId(),
            event.paymentId(),
            event.userId(),
            products,
            LocalDateTime.now()
        );
    }
}

