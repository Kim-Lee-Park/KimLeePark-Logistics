package com.klp.user.presentation.dto.response;

public record UserInfoResponse(
    Long id,
    String username,
    String slackId,
    String affiliationName,
    String status
) {

}
