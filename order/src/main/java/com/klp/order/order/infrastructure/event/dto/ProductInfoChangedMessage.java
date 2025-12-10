package com.klp.order.order.infrastructure.event.dto;

import java.util.UUID;

public record ProductInfoChangedMessage(
    UUID productId,
    String eventType
) {

}
