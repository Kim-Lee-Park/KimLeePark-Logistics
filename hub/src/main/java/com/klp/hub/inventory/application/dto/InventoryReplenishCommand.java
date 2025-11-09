package com.klp.hub.inventory.application.dto;

import java.util.List;
import java.util.UUID;

public record InventoryReplenishCommand(
    String idempotencyKey,
    List<Product> products
) {

    public record Product(
        UUID productId,
        UUID hubId,
        Integer quantity
    ) {

    }

    public int size() {
        return products.size();
    }
}
