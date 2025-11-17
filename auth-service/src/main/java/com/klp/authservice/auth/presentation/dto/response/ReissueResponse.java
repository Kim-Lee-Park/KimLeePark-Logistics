package com.klp.authservice.auth.presentation.dto.response;

public record ReissueResponse(
    Long userId,
    String username,
    String role,
    String accessToken
) {

}
