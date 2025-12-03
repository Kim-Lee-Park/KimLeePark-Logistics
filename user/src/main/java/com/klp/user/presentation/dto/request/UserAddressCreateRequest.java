package com.klp.user.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.klp.user.application.command.UserAddressCreateCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record UserAddressCreateRequest(
    @NotNull(message = "허브 ID는 필수 입력값입니다")
    UUID hubId,
    @NotBlank(message = "주소는 필수 입력값입니다")
    String address,
    String detail,
    @JsonProperty("is_default")
    boolean isDefault,
    @NotNull(message = "위도는 필수 입력값입니다")
    Double latitude,
    @NotNull(message = "경도는 필수 입력값입니다")
    Double longitude
) {

    public UserAddressCreateCommand toCommand(Long userId) {
        return new UserAddressCreateCommand(userId, hubId, address, detail, isDefault, latitude, longitude);
    }
}
