package com.klp.order.infrastructure.client.dto.delivery.request;

import java.util.List;
import java.util.UUID;

public record CreateDeliveryRequest(
    UUID orderId,
    String idempotencykey,
    Long supplierId,
    Long customerId,
    List<DeliveryOrderItem> orderItems
) {

    public record DeliveryOrderItem(
        UUID orderItemId,
        UUID hubId
    ) {

    }
}
