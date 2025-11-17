package com.klp.order.common.event.presentation.controller.dto.response;

import com.klp.order.common.event.application.service.dto.EventStoreDto;
import java.time.LocalDateTime;
import java.util.UUID;

public record EventStoreResponse(
    UUID eventStoreId,
    String eventType,
    Object payload,
    LocalDateTime publishedAt
) {
    public static EventStoreResponse from(EventStoreDto dto) {
        return new EventStoreResponse(
            dto.eventStoreId(),
            dto.eventType(),
            dto.payload(),
            dto.publishedAt()
        );
    }
}
