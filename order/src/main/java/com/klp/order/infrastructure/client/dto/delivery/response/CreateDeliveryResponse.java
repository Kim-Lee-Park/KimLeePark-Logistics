package com.klp.order.infrastructure.client.dto.delivery.response;

import java.util.List;
import java.util.UUID;

public record CreateDeliveryResponse(
    UUID orderId,
    List<DeliveryItem> items
) {

    public record DeliveryItem(
        UUID orderItemId,
        UUID deliveryId
    ) {

    }
}
