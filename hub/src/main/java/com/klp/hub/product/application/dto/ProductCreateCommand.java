package com.klp.hub.product.application.dto;

import java.util.UUID;

public record ProductCreateCommand(
        UUID companyId,
        UUID hubId,
        String name,
        Integer quantity
) {
}
