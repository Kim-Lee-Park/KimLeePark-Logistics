package com.klp.order.outbox.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.order.common.event.EventPublisher;
import com.klp.order.order.domain.event.OrderCreatedEvent;
import com.klp.order.outbox.domain.entity.OutboxEvent;
import com.klp.order.outbox.domain.entity.OutboxStatus;
import com.klp.order.outbox.infrastructure.repository.OutboxRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisherScheduler {

    private final ApplicationEventPublisher eventPublisher;

    private final OutboxRepository outboxRepository;

    private final ObjectMapper objectMapper;

    @Transactional
    @Scheduled(fixedDelay = 1000)
    public void publish() {
        List<OutboxEvent> outboundEvents = outboxRepository.findAllByStatus(OutboxStatus.PENDING);

        if (outboundEvents.isEmpty()) return;

        outboundEvents.forEach(outboxEvent -> {
            if (outboxEvent.isOrderCreatedEvent()) {
                try {
                    OrderCreatedEvent event = objectMapper.readValue(outboxEvent.getPayload(), OrderCreatedEvent.class);
                    eventPublisher.publishEvent(event);
                    outboxEvent.published();
                    outboxRepository.save(outboxEvent);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
