package com.klp.hub.hub.presentation.dto.response;

import java.util.List;
import java.util.UUID;

public record GetHubListResponse(
    List<HubSummaryResponse> hubs,
    PageableDto pageable
) {
    public record HubSummaryResponse(
        UUID hubId,
        String name,
        Long latitude,
        Long longitude,
        String address
    ){}

    public record PageableDto(
        Integer page,
        Integer size,
        Integer totalElements,
        Integer totalPages,
        Boolean hasNext,
        Boolean hasPrevious,
        Boolean isFirst,
        Boolean isLast
    ) {

    }
}
