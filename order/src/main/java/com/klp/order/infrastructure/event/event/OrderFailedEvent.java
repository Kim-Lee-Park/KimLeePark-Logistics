package com.klp.order.infrastructure.event.event;

import java.util.UUID;

public record OrderFailedEvent(
    UUID orderId,
    UUID userCouponId,
    WhichRollback type
) {

}
