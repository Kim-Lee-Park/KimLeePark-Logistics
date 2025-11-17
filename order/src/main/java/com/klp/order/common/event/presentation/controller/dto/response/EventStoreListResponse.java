package com.klp.order.common.event.presentation.controller.dto.response;

import com.klp.order.common.event.application.service.dto.EventStoreDto;
import java.util.List;

public record EventStoreListResponse(
    List<EventStoreResponse> list
) {
    public static EventStoreListResponse from(List<EventStoreDto> list) {
        List<EventStoreResponse> responseList = list.stream().map(EventStoreResponse::from).toList();
        return new EventStoreListResponse(responseList);
    }
}
