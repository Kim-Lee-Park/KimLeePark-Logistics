package com.klp.authservice.auth.entrypoint.dto.response;

public record ReissueResponse(
    Long userId,
    String userName,
    String role,
    String accessToken
) {

}
