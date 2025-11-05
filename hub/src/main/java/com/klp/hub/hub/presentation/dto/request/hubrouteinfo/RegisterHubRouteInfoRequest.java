package com.klp.hub.hub.presentation.dto.request.hubrouteinfo;

import java.util.UUID;

public record RegisterHubRouteInfoRequest(
    UUID departureId,
    UUID arrivalId,
    Long duration_min,
    Double distance_km
) {

}
