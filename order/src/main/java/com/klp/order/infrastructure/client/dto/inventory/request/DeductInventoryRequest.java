package com.klp.order.infrastructure.client.dto.inventory.request;

import java.util.List;
import java.util.UUID;

public record DeductInventoryRequest(
    String idempotencyKey,
    List<ProductDeduction> products
) {

    public record ProductDeduction(
        UUID productId,
        UUID hubId,
        Integer quantity
    ) {

    }
}
