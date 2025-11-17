package com.klp.order.common.event.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.order.common.event.DomainEvent;
import com.klp.order.common.event.domain.EventStore;
import com.klp.order.common.event.domain.EventType;
import com.klp.order.common.event.infrastructure.repository.EventStoreRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventStoreService {

    private final EventStoreRepository eventStoreRepository;

    private final ObjectMapper objectMapper;

    @Transactional
    public UUID saveEvent(DomainEvent event, EventType eventType, LocalDateTime publishedAt) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            EventStore savedEventStore = eventStoreRepository.save(
                new EventStore(payload, eventType, publishedAt)
            );
            return savedEventStore.getEventStoreId();
        } catch (JsonProcessingException e) {
            log.error("이벤트 객체 직렬화 실패 error : {}", e.getMessage(), e);
            throw new RuntimeException("이벤트 객체 직렬화에 실패하였습니다.");
        }
    }
}
