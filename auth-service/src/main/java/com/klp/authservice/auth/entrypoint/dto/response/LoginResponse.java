package com.klp.authservice.auth.entrypoint.dto.response;

public record LoginResponse(
    Long userId,
    String userName,
    String role,
    String accessToken
) {

}
