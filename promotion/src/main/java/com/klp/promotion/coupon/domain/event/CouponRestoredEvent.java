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
}


