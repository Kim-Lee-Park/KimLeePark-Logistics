package com.klp.delivery.delivery.presentation.dto;

import java.util.List;
import java.util.UUID;

public record DeliveryResponse(
    UUID orderId,
    List<DeliveryItemResponse> items
) {

    public record DeliveryItemResponse(
        UUID orderItemId,
        UUID deliveryId
    ) {

    }
}
