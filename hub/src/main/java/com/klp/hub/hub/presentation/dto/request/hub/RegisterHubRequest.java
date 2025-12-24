package com.klp.hub.hub.presentation.dto.request.hub;

import com.klp.hub.hub.application.command.hub.RegisterHubCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "허브 등록 요청 DTO")
public record RegisterHubRequest(

    @Schema(
        description = "허브 이름",
        example = "제주허브"
    )
    @NotBlank(message = "허브 이름은 필수입니다.")
    String name,

    @Schema(
        description = "허브 위도 (-90 ~ 90)",
        example = "33.4996"
    )
    @NotNull(message = "위도는 필수입니다.")
    @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
    @DecimalMax(value = "90.0", message = "위도는 90 이하이어야 합니다.")
    Double latitude,

    @Schema(
        description = "허브 경도 (-180 ~ 180)",
        example = "126.5312"
    )
    @NotNull(message = "경도는 필수입니다.")
    @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
    @DecimalMax(value = "180.0", message = "경도는 180 이하이어야 합니다.")
    Double longitude,

    @Schema(
        description = "허브 주소",
        example = "서울특별시 송파구 송파대로 55"
    )
    @NotBlank(message = "주소는 필수입니다.")
    String address
) {

    public RegisterHubCommand toCommand() {
        return new RegisterHubCommand(
            name,
            latitude,
            longitude,
            address
        );
    }
}