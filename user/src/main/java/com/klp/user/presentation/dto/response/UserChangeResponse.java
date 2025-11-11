package com.klp.user.presentation.dto.response;

public record UserChangeResponse(
    Long requestId,
    String username,
    String affiliationName
) {

}
