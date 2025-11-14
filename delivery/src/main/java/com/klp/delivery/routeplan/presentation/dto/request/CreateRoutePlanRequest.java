package com.klp.delivery.routeplan.presentation.dto.request;

import com.klp.delivery.routeplan.application.command.CreateRoutePlanCommand;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateRoutePlanRequest(
    @NotNull(message = "출발 허브 ID는 필수입니다.")
    UUID departureId,
    @NotNull(message = "도착 허브 ID는 필수입니다.")
    UUID arrivalId
) {
    public CreateRoutePlanCommand toCommand(){
        return new CreateRoutePlanCommand(departureId, arrivalId);
    }
}
