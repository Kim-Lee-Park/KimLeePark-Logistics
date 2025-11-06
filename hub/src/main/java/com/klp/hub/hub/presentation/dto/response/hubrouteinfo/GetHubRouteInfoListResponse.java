package com.klp.hub.hub.presentation.dto.response.hubrouteinfo;

import java.util.List;
import java.util.UUID;

public record GetHubRouteInfoListResponse(
    List<HubRouteInfoSummaryResponse> routeInfos,
    PageableDto pageable
) {
    public record HubRouteInfoSummaryResponse(
        UUID routeInfoId,
        UUID departureId,
        UUID arrivalId,
        Long durationMin,
        Double distanceKm
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
    ){}
}
