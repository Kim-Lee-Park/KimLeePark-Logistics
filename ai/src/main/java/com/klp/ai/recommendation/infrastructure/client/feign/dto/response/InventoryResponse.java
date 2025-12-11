package com.klp.ai.recommendation.infrastructure.client.feign.dto.response;

import java.util.UUID;

public record InventoryResponse(
    UUID productId,
    UUID inventoryId,
    UUID hubId,
    Integer quantity
) {

}
