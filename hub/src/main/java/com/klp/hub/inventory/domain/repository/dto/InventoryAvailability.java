package com.klp.hub.inventory.domain.repository.dto;

import java.util.UUID;

public record InventoryAvailability(
    UUID productId,
    UUID hubId,
    Integer availableQuantity
) {

}
