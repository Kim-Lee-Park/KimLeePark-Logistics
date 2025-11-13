package com.klp.hub.inventory.domain.repository.dto;

import java.util.UUID;

public record InventoryReplenish(
    UUID productId,
    UUID hubId,
    Integer quantity
) {

}
