package com.klp.user.application.command;

public record ValidateUserCommand(
    String username,
    String password
) {

}
