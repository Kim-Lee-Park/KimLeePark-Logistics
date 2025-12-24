package com.klp.authservice.auth.presentation.dto.response;

public record LoginResponse(
    Long userId,
    String username,
    String role,
    String accessToken
) {

}
