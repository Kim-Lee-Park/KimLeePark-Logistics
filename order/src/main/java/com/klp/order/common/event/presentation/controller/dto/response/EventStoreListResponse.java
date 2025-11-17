package com.klp.order.common.event.presentation.controller.dto.response;

import java.util.List;

public record EventStoreListResponse(
    List<EventStoreResponse> list
) {

}
