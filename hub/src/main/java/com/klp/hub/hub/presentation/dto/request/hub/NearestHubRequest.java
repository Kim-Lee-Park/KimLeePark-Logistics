package com.klp.hub.hub.presentation.dto.request.hub;

import com.klp.hub.hub.application.command.hub.NearestHubCommand;
import jakarta.validation.constraints.NotNull;

public record NearestHubRequest(
    @NotNull(message = "위도는 필수입니다.")
    Double latitude,
    @NotNull(message = "경도는 필수입니다.")
    Double longitude
) {

    public NearestHubCommand toCommand() {
        return new NearestHubCommand(latitude, longitude);
    }
}
