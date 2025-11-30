package com.klp.product.presentation.controller.dto;

import java.util.UUID;

public record ProductListResponse(
    UUID productId,
    String name
) {

}
