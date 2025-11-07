package com.klp.hub.inventory.application.dto;

import com.klp.hub.inventory.domain.repository.dto.InventoryReplenish;
import java.util.List;
import java.util.UUID;

public record InventoryReplenishCommand(
    String idempotencyKey,
    List<Product> products
) {

    public List<InventoryReplenish> toInventoryReplenish() {
        return products.stream().map(Product::toInventoryReplenish).toList();
    }

    public record Product(
        UUID productId,
        UUID hubId,
        Integer quantity
    ) {

        public InventoryReplenish toInventoryReplenish() {
            return new InventoryReplenish(
                productId,
                hubId,
                quantity
            );
        }
    }

    public int size() {
        return products.size();
    }
}
