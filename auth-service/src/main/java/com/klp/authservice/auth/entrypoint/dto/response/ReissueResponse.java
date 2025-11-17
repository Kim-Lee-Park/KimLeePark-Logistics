package com.klp.authservice.auth.entrypoint.dto.response;

public record ReissueResponse(
    Long userId,
    String username,
    String role,
    String accessToken
) {

}
