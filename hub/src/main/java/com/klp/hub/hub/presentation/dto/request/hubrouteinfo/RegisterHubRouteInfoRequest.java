package com.klp.hub.hub.presentation.dto.request.hubrouteinfo;

import com.klp.hub.hub.application.command.hubRouteInfo.RegisterHubRouteInfoCommand;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RegisterHubRouteInfoRequest(
    @NotNull(message = "출발 허브 ID는 필수입니다.")
    UUID departureId,
    @NotNull(message = "도착 허브 ID는 필수입니다.")
    UUID arrivalId
) {
    public RegisterHubRouteInfoCommand toCommand(){
        return new RegisterHubRouteInfoCommand(departureId, arrivalId);
    }
}
