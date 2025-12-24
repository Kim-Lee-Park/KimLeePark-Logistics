package com.klp.hub.inventory.presentation.dto.response;

import java.util.UUID;

public record HotProductQuantityResponse(
    UUID productId,
    UUID hubId,
    int quantity
) {
    public static HotProductQuantityResponse of(UUID productId, UUID hubId, int quantity) {
        return new HotProductQuantityResponse(productId, hubId, quantity);
    }
}
