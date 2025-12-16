package com.klp.promotion.coupon.domain.event;

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
}
