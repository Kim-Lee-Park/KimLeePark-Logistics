package com.klp.payment.payment.domain.event;

import java.util.UUID;

public record OrderFailedEvent(
    UUID orderId,
    UUID userCouponId,
    WhichRollback type
) {

}
