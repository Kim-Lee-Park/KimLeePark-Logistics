package com.klp.authservice.auth.application.command;

public record LoginCommand(
    String username,
    String password
) {

}
