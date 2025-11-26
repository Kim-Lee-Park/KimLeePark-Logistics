package com.klp.authservice.auth.infrastructure.external.dto.response;

public record UserDataResponse(
    Long userId,
    String userName,
    String password,
    String role
) {

}
