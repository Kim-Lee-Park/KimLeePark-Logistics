package com.klp.order.common.event.presentation.controller.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventStoreResponse(
    UUID eventStoreId,
    String eventType,
    Object payload,
    LocalDateTime publishedAt
) {

}
