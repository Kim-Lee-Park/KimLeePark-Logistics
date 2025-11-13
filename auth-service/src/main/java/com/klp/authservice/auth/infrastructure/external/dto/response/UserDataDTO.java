package com.klp.authservice.auth.infrastructure.external.dto.response;

public record UserDataDTO(
    Long userId,
    String userName,
    String password,
    String role
) {

}
