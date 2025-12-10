package com.klp.order.order.infrastructure.event.dto;

public record UserProfileChangedMessage(
    Long userId,
    String eventType
) {

}
