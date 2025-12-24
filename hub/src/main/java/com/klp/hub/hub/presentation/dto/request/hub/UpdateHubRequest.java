package com.klp.hub.hub.presentation.dto.request.hub;

import com.klp.hub.hub.application.command.hub.UpdateHubCommand;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "허브 수정 요청 DTO")
public record UpdateHubRequest(

    @Schema(
        description = "허브 이름",
        example = "제주 2센터",
        nullable = true
    )
    String name,

    @Schema(
        description = "허브 위도 (-90 ~ 90)",
        example = "33.5001",
        nullable = true
    )
    Double latitude,

    @Schema(
        description = "허브 경도 (-180 ~ 180)",
        example = "126.5320",
        nullable = true
    )
    Double longitude,

    @Schema(
        description = "허브 주소",
        example = "제주특별자치도 제주시 연동 123-45",
        nullable = true
    )
    String address
) {

    public UpdateHubCommand toCommand() {
        return new UpdateHubCommand(
            name,
            latitude,
            longitude,
            address
        );
    }
}
