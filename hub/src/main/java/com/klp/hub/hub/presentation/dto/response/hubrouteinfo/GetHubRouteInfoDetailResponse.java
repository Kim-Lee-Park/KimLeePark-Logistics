package com.klp.hub.hub.presentation.dto.response.hubrouteinfo;

import com.klp.hub.hub.domain.model.HubRouteInfo;
import java.util.UUID;

public record GetHubRouteInfoDetailResponse(
    UUID routeInfoId,
    UUID departureId,
    UUID arrivalId,
    Long durationMin,
    Double distanceKm
) {
    public static GetHubRouteInfoDetailResponse from(HubRouteInfo hubRouteInfo) {
        return new GetHubRouteInfoDetailResponse(
            hubRouteInfo.getHubRouteId(),
            hubRouteInfo.getDepartureId(),
            hubRouteInfo.getArrivalId(),
            hubRouteInfo.getDurationMin(),
            hubRouteInfo.getDistanceKm()
        );
    }
}
