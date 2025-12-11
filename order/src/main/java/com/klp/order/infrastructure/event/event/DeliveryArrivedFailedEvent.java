package com.klp.order.infrastructure.event.event;

import java.util.UUID;

public record DeliveryArrivedFailedEvent(
    UUID orderId) {

}
