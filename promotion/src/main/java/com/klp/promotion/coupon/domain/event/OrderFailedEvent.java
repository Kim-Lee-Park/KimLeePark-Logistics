package com.klp.promotion.coupon.domain.event;

import java.util.UUID;

public record OrderFailedEvent(
    UUID orderId,
    UUID userCouponId
) {
}
