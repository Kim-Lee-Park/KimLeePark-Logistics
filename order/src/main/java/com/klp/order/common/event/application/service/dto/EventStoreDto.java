package com.klp.order.common.event.application.service.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventStoreDto(
    UUID eventStoreId,
    String eventType,
    Object payload,
    LocalDateTime publishedAt
) {

}
