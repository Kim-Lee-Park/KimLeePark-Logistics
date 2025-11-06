package com.klp.hub.hub.presentation.dto.request.hubrouteinfo;

import com.klp.hub.hub.application.command.hubRouteInfo.UpdateHubRouteInfoCommand;
import java.util.UUID;

public record UpdateHubRouteInfoRequest(
    UUID departureId,
    UUID arrivalId,
    Long durationMin,
    Double distanceKm
) {
    public UpdateHubRouteInfoCommand toCommand() {
        return new UpdateHubRouteInfoCommand(departureId, arrivalId, durationMin, distanceKm);
    }
}
