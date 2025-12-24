package com.klp.delivery.delivery.domain.event;

import java.util.UUID;

public record DeliveryArrivedFailedEvent(
    UUID orderId
) {

}
