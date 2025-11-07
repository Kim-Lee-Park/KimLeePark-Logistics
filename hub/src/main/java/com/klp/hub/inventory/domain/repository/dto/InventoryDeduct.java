package com.klp.hub.inventory.domain.repository.dto;

import java.util.UUID;

public record InventoryDeduct(
    UUID productId,
    UUID hubId,
    Integer quantity
) {

}
