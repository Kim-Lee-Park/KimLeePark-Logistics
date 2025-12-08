package com.klp.delivery.delivery.domain.event;

import java.util.List;
import java.util.UUID;

public record OrderDeliveryEvent(
    UUID orderId,
    String status,
    List<DeliveryItem> items
) {

    public record DeliveryItem(
        UUID orderItemId,
        UUID deliveryId
    ) {

    }

}
