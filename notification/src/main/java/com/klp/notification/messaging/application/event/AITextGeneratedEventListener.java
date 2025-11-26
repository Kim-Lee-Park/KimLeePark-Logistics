package com.klp.notification.messaging.application.event;

import com.klp.notification.ai.domain.event.AITextGeneratedEvent;
import com.klp.notification.messaging.domain.MessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * AI 텍스트 생성 완료 이벤트 리스너 AI 텍스트 생성이 완료되면 사용자에게 알림을 전송합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AITextGeneratedEventListener {

    private final MessageSender messageSender;

    @Async
    @EventListener
    public void handleAITextGeneratedEvent(AITextGeneratedEvent event) {
        log.info("AI 텍스트 생성 완료 이벤트 수신: recipientId={}", event.getRecipientId());

        messageSender.sendMessage(event.getRecipientId(), event.getGeneratedText());

        log.info("알림 메시지 전송 완료: recipientId={}", event.getRecipientId());
    }
}
