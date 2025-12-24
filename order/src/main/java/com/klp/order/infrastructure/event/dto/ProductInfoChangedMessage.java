package com.klp.order.infrastructure.event.dto;

import java.util.UUID;

public record ProductInfoChangedMessage(
    UUID productId,
    String eventType
) {

}
