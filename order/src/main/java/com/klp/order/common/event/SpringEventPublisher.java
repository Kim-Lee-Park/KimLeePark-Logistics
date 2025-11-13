package com.klp.order.common.event;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SpringEventPublisher implements EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(DomainEvent event) {
        log.info("발행 시작 event ID : {} occurredAt : {}", event.getEventId(), event.getOccurredAt());
        applicationEventPublisher.publishEvent(event);
        log.info("발행 완료 event ID :{} publishedAt : {}", event.getEventId(), LocalDateTime.now());
    }
}
