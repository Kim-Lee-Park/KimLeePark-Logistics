package com.klp.order.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.order.domain.entity.outbox.OrderOutboxEvent;
import com.klp.order.domain.repository.OrderOutboxEventRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class OrderOutboxEventService {

    private final OrderOutboxEventRepository orderOutboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void saveEvent(String aggregateType, UUID aggregateId,
        String eventType, Object eventData) {
        try {
            String payload = objectMapper.writeValueAsString(eventData);

            OrderOutboxEvent outboxEvent = OrderOutboxEvent.create(
                aggregateType,
                aggregateId,
                eventType,
                payload
            );

            orderOutboxEventRepository.save(outboxEvent);
            log.info("Outbox 이벤트 저장 완료: eventType={}, aggregateId={}",
                eventType, aggregateId);

        } catch (Exception e) {
            log.error("Outbox 이벤트 저장 실패", e);
            throw new RuntimeException("Outbox 이벤트 저장 실패", e);
        }
    }
}
