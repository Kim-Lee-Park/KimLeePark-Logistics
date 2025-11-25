package com.klp.authservice.auth.presentation.dto.request;

import com.klp.authservice.auth.application.command.LoginCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(
    @NotBlank(message = "사용자 이름은 필수입니다")
    @Pattern(regexp = "^[a-z0-9]{4,10}$", message = "유저 이름은 4자 이상, 10자 이하이며 알파벳 소문자, 숫자로만 구성 가능합니다")
    String username,

    @NotBlank(message = "비밀번호는 필수입니다")
    String password
) {

    public LoginCommand toCommand() {
        return new LoginCommand(username, password);
    }
}
