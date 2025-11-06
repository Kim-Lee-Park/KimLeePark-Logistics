package com.klp.hub.hub.presentation.dto.response.hubrouteinfo;

import java.util.UUID;

public record UpdatedHubRouteInfoResponse(
    UUID routeInfoId,
    UUID departureId,
    UUID arrivalId,
    Long durationMin,
    Double distanceKm
) {

}
