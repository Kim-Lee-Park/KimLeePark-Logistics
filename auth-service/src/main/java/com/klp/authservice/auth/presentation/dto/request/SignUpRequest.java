package com.klp.authservice.auth.presentation.dto.request;

import com.klp.authservice.auth.application.command.SignUpCommand;
import com.klp.authservice.auth.domain.enums.AffiliationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record SignUpRequest(
    @NotBlank(message = "유저 이름은 필수 입니다")
    @Pattern(regexp = "^[a-z0-9]{4,10}$", message = "유저 이름은 4자 이상, 10자 이하이며 알파벳 소문자, 숫자로만 구성 가능합니다")
    String username,

    @NotBlank(message = "패스워드는 필수 입니다")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\|,.<>/?])[A-Za-z\\d!@#$%^&*()_+\\-=\\[\\]{};':\"\\|,.<>/?]{8,15}$",
        message = "패스워드는 8자 이상, 15자 이하이며 알파벳 대소문자, 숫자, 특수문자가 포함되어야 합니다"
    )
    String password,

    @NotBlank(message = "슬랙 ID는 필수 입니다")
    String slackId,

    @NotBlank(message = "전화번호는 필수 입니다")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다")
    String phone,

    @NotBlank(message = "소속 업체명(또는 허브명)은 필수 입니다")
    String affiliationName,

    @NotNull(message = "업체 타입은 필수입니다")
    AffiliationType affiliationType
) {

    public SignUpCommand toCommand() {
        return new SignUpCommand(username, password, slackId, affiliationName, affiliationType);
    }
}
