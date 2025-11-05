package com.klp.hub.hub.application.command.hubRouteInfo;

import java.util.UUID;

public record RegisterHubRouteInfoCommand(
    UUID departureId,
    UUID arrivalId,
    Long duration_min,
    Double distance_km
) {

}
