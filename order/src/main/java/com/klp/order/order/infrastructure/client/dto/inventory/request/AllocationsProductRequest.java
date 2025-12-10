package com.klp.order.order.infrastructure.client.dto.inventory.request;

import java.util.UUID;

public record AllocationsProductRequest(
    UUID productId,
    int quantity

) {

}
