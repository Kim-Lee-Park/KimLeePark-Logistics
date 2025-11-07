package com.klp.hub.product.presentation.dto;

import java.util.UUID;

public record ProductResponse(
        UUID productId,
        UUID hubId,
        String companyName,
        String productName
) {
}
