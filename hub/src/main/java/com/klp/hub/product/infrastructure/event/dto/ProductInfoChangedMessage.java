package com.klp.hub.product.infrastructure.event.dto;

import java.util.UUID;

public record ProductInfoChangedMessage(
    UUID productId,
    String eventType
) {

}
