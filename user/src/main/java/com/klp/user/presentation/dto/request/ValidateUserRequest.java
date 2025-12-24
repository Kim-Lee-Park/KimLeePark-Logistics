package com.klp.user.presentation.dto.request;

import com.klp.user.application.command.ValidateUserCommand;

public record ValidateUserRequest(
    String username,
    String password
) {

    public ValidateUserCommand toCommand() {
        return new ValidateUserCommand(
            username,
            password
        );
    }
}
