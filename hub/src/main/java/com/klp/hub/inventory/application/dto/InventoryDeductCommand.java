package com.klp.hub.inventory.application.dto;

import com.klp.hub.inventory.domain.repository.dto.InventoryDeduct;
import java.util.List;
import java.util.UUID;

public record InventoryDeductCommand(
    String idempotencyKey,
    List<Product> products
) {

    public record Product(
        UUID productId,
        UUID hubId,
        Integer quantity
    ) {

        public InventoryDeduct toInventoryDeduct() {
            return new InventoryDeduct(
                productId,
                hubId,
                quantity
            );
        }
    }
}
