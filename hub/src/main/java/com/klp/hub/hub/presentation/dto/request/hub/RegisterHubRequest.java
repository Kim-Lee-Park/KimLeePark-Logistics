package com.klp.hub.hub.presentation.dto.request.hub;

import com.klp.hub.hub.application.command.hub.RegisterHubCommand;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterHubRequest(
    @NotBlank(message = "허브 이름은 필수입니다.")
    String name,

    @NotNull(message = "위도는 필수입니다.")
    @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
    @DecimalMax(value = "90.0", message = "위도는 90 이하이어야 합니다.")
    Double latitude,

    @NotNull(message = "경도는 필수입니다.")
    @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
    @DecimalMax(value = "180.0", message = "경도는 180 이하이어야 합니다.")
    Double longitude,

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