package com.klp.hub.hub.presentation.dto.request.hubrouteinfo;

import com.klp.hub.hub.application.command.hubRouteInfo.UpdateHubRouteInfoCommand;
import java.util.UUID;

public record UpdateHubRouteInfoRequest(
    UUID departureId,
    UUID arrivalId,
    Long duration_min,
    Double distance_km
) {
    public UpdateHubRouteInfoCommand toCommand() {
        return new UpdateHubRouteInfoCommand(departureId, arrivalId, duration_min, distance_km);
    }
}
