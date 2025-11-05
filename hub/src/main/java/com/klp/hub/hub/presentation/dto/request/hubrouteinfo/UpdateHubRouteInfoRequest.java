package com.klp.hub.hub.presentation.dto.request.hubrouteinfo;

import java.util.UUID;

public record UpdateHubRouteInfoRequest(
    UUID departureId,
    UUID arrivalId,
    Long duration_min,
    Double distance_km
) {

}
