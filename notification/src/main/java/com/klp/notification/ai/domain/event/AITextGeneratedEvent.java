package com.klp.notification.ai.domain.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AITextGeneratedEvent extends ApplicationEvent {

    private final String recipientId;
    private final String generatedText;

    public AITextGeneratedEvent(Object source, String recipientId, String generatedText) {
        super(source);
        this.recipientId = recipientId;
        this.generatedText = generatedText;
    }
}
