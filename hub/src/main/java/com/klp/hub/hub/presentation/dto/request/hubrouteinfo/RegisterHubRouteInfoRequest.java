package com.klp.hub.hub.presentation.dto.request.hubrouteinfo;

import com.klp.hub.hub.application.command.hubRouteInfo.RegisterHubRouteInfoCommand;
import java.util.UUID;

public record RegisterHubRouteInfoRequest(
    UUID departureId,
    UUID arrivalId,
    Long durationMin,
    Double distanceKm
) {
    public RegisterHubRouteInfoCommand toCommand(){
        return new RegisterHubRouteInfoCommand(departureId, arrivalId, durationMin, distanceKm);
    }
}
