package com.klp.user.presentation.dto.response;

public record UserDetailResponse(
    String affiliationName,
    String username,
    String role,
    boolean activate
) {

}
