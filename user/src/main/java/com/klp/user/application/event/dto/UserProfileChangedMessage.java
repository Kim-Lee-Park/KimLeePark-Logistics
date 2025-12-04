package com.klp.user.application.event.dto;

public record UserProfileChangedMessage(
    Long userId,
    String changeType
) {

}
