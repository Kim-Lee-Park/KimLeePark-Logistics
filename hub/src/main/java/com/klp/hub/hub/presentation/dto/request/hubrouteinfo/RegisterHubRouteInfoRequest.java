package com.klp.hub.hub.presentation.dto.request.hubrouteinfo;

import com.klp.hub.hub.application.command.hubRouteInfo.RegisterHubRouteInfoCommand;
import java.util.UUID;

public record RegisterHubRouteInfoRequest(
    UUID departureId,
    UUID arrivalId,
    Long duration_min,
    Double distance_km
) {
    public RegisterHubRouteInfoCommand toCommand(){
        return new RegisterHubRouteInfoCommand(departureId, arrivalId, duration_min, distance_km);
    }
}
