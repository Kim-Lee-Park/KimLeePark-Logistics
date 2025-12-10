package com.klp.order.order.infrastructure.event.event;

import java.util.UUID;

public record DeliveryArrivedFailedEvent(
    UUID orderId) {

}
