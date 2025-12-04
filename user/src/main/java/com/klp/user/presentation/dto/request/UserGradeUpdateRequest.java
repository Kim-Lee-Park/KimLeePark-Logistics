package com.klp.user.presentation.dto.request;

import com.klp.user.application.command.UpdateUserGradeCommand;
import jakarta.validation.constraints.NotBlank;

public record UserGradeUpdateRequest(
    @NotBlank(message = "등급 이름은 필수입니다.")
    String gradeName
) {

    public UpdateUserGradeCommand toCommand(Long userId) {
        return new UpdateUserGradeCommand(userId, gradeName);
    }
}
