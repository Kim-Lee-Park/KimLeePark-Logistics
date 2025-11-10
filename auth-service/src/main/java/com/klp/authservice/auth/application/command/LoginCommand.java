package com.klp.authservice.auth.application.command;

public record LoginCommand(
    String userName,
    String password
) {

}
