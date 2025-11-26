package com.klp.notification.ai.application.event;

import com.klp.notification.ai.domain.MessageSender;
import com.klp.notification.ai.domain.event.AITextGeneratedEvent;
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

        String message = formatMessage(event.getGeneratedText());
        messageSender.sendMessage(event.getRecipientId(), message);

        log.info("알림 메시지 전송 완료: recipientId={}", event.getRecipientId());
    }

    private String formatMessage(String generatedText) {
        return String.format("""
            %s
            
            배송 계획을 확인해주세요!
            """, generatedText);
    }
}
