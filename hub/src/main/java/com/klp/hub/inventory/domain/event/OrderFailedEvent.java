package com.klp.hub.inventory.domain.event;

import java.util.UUID;

public record OrderFailedEvent(
    UUID orderId,
    UUID userCouponId,
    WhichRollback type
) {

}
