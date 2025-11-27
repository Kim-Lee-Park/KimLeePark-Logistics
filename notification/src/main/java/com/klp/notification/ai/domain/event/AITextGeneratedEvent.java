package com.klp.notification.ai.domain.event;

public record AITextGeneratedEvent(
    String recipientId,
    String generatedText
) {

}
