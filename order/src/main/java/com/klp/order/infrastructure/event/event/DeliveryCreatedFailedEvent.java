package com.klp.order.infrastructure.event.event;

import java.util.List;
import java.util.UUID;

public record DeliveryCreatedFailedEvent(
    UUID orderId,
    List<DeliveryItem> items
) {

    public record DeliveryItem(
        UUID orderItemId,
        UUID deliveryId
    ) {

    }
}
