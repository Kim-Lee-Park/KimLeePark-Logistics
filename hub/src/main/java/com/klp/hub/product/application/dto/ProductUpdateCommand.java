package com.klp.hub.product.application.dto;

import java.util.UUID;

public record ProductUpdateCommand(
        UUID productId,
        String name
) {
}
