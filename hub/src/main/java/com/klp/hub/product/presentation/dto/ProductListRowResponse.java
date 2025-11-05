package com.klp.hub.product.presentation.dto;

import java.util.UUID;

public record ProductListRowResponse(
        UUID productId,
        UUID hubId,
        String companyName,
        String productName
) {
}
