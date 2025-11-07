package com.klp.hub.inventory.presentation.dto;

import com.klp.hub.inventory.application.dto.InventoryDeductCommand;
import java.util.List;
import java.util.UUID;

public record InventoryDeductRequest(
    String idempotencyKey,
    List<Product> products
) {

    public InventoryDeductCommand toCommand() {
        return new InventoryDeductCommand(
            idempotencyKey,
            products.stream().map(product -> new InventoryDeductCommand.Product(
                product.productId,
                product.hubId,
                product.quantity
            )).toList()
        );
    }

    public record Product(
        UUID productId,
        UUID hubId,
        Integer quantity
    ) {

    }
}
