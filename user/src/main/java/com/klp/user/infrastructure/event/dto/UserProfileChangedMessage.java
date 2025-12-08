package com.klp.user.infrastructure.event.dto;

public record UserProfileChangedMessage(
    Long userId,
    String eventType
) {

}
