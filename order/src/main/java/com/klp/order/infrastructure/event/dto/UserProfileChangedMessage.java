package com.klp.order.infrastructure.event.dto;

public record UserProfileChangedMessage(
    Long userId,
    String eventType
) {

}
