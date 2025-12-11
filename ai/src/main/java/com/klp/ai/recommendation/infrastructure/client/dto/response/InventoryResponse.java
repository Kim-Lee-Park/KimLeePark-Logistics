package com.klp.ai.recommendation.infrastructure.client.dto.response;

import java.util.UUID;

public record InventoryResponse(
    UUID productId,
    UUID inventoryId,
    UUID hubId,
    Integer quantity
) {

}
