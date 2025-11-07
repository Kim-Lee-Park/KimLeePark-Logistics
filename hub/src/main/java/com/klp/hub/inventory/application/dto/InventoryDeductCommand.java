package com.klp.hub.inventory.application.dto;

import java.util.List;
import java.util.UUID;

public record InventoryDeductCommand(
    String idempotencyKey,
    List<Product> products
) {

    public record Product(
        UUID productId,
        Integer quantity
    ) {

    }
}
