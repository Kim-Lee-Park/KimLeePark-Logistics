package com.klp.hub.inventory.presentation.dto.response;

import java.util.UUID;

public record InventoryResponse(
    UUID productId,
    UUID inventoryId,
    UUID hubId,
    Integer quantity
) {

}
