package com.klp.product.presentation.controller.dto;

import java.util.UUID;

public record ProductResponse(
    UUID productId,
    String version
) {

}
