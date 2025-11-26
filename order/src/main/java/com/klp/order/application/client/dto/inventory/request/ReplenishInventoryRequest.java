package com.klp.order.application.client.dto.inventory.request;

import java.util.List;
import java.util.UUID;

public record ReplenishInventoryRequest(
    String idempotencyKey,
    List<ProductReplenishment> products
) {

    public record ProductReplenishment(
        UUID productId,
        UUID hubId,
        Integer quantity
    ) {

    }
}
