package com.klp.user.presentation.dto.request;

public record UserChangeRequest(
    String username,
    String password,
    String slackId,
    String affiliationName
) {

}
