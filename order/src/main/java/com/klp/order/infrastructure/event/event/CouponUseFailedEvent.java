package com.klp.order.infrastructure.event.event;

import java.util.UUID;

public record CouponUseFailedEvent(
    UUID orderId,
    UUID couponId
) {

}
