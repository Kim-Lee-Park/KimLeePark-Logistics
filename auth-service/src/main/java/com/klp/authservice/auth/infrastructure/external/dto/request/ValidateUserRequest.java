package com.klp.authservice.auth.infrastructure.external.dto.request;

public record ValidateUserRequest(
    String username,
    String password
) {

}
