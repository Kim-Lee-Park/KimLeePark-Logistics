package com.klp.hub.inventory.presentation.dto;

import java.util.UUID;

public record InventoryResponse(
        UUID productId,
        Integer quantity
) {
}
