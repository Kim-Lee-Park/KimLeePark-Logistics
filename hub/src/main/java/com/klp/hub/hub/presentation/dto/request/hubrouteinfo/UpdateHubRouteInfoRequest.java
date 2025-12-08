package com.klp.hub.hub.presentation.dto.request.hubrouteinfo;

import com.klp.hub.hub.application.command.hubRouteInfo.UpdateHubRouteInfoCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "허브 간 이동 정보 수정 요청 DTO")
public record UpdateHubRouteInfoRequest(

    @Schema(
        description = "출발 허브 ID",
        example = "550e8400-e29b-41d4-a716-446655440000",
        nullable = true
    )
    UUID departureId,

    @Schema(
        description = "도착 허브 ID",
        example = "550e8400-e29b-41d4-a716-446655440111",
        nullable = true
    )
    UUID arrivalId,

    @Schema(
        description = "이동 소요 시간 (분 단위)",
        example = "45",
        nullable = true
    )
    Long durationMin,

    @Schema(
        description = "이동 거리 (km)",
        example = "12.8",
        nullable = true
    )
    Double distanceKm
) {

    public UpdateHubRouteInfoCommand toCommand() {
        return new UpdateHubRouteInfoCommand(
            departureId,
            arrivalId,
            durationMin,
            distanceKm
        );
    }
}
