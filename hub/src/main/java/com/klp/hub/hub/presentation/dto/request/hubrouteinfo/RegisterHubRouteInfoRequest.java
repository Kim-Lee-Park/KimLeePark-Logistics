package com.klp.hub.hub.presentation.dto.request.hubrouteinfo;

import com.klp.hub.hub.application.command.hubRouteInfo.RegisterHubRouteInfoCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "허브 간 이동 정보 등록 요청 DTO")
public record RegisterHubRouteInfoRequest(

    @Schema(
        description = "출발 허브 ID",
        example = "550e8400-e29b-41d4-a716-446655440000"
    )
    @NotNull(message = "출발 허브 ID는 필수입니다.")
    UUID departureId,

    @Schema(
        description = "도착 허브 ID",
        example = "550e8400-e29b-41d4-a716-446655440111"
    )
    @NotNull(message = "도착 허브 ID는 필수입니다.")
    UUID arrivalId
) {

    public RegisterHubRouteInfoCommand toCommand() {
        return new RegisterHubRouteInfoCommand(departureId, arrivalId);
    }
}
