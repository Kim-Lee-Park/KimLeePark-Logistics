package com.klp.delivery.routeplan.presentation.dto.request;

import com.klp.delivery.routeplan.application.command.CreateRoutePlanCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "경로 계획 생성 요청 DTO")
public record CreateRoutePlanRequest(

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

    public CreateRoutePlanCommand toCommand() {
        return new CreateRoutePlanCommand(departureId, arrivalId);
    }
}
