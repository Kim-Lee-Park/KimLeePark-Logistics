package com.klp.order.common.event.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.order.common.event.DomainEvent;
import com.klp.order.common.event.application.service.dto.EventStoreDto;
import com.klp.order.common.event.domain.EventStore;
import com.klp.order.common.event.domain.EventType;
import com.klp.order.common.event.infrastructure.repository.EventStoreRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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

    @Transactional(readOnly = true)
    public List<EventStoreDto> findAll() {
        List<EventStore> eventStoreList = eventStoreRepository.findAll();

        return eventStoreList
            .stream()
            .map(eventStore ->
                new EventStoreDto(
                    eventStore.getEventStoreId(),
                    eventStore.getEventType().name(),
                    deserialize(eventStore.getPayload()),
                    eventStore.getPublishedAt()
                )
            ).toList();
    }

    @Transactional
    public UUID saveEvent(DomainEvent event, EventType eventType, LocalDateTime publishedAt) {
        String payload = serialize(event);
        EventStore savedEventStore = eventStoreRepository.save(
            new EventStore(payload, eventType, publishedAt)
        );
        return savedEventStore.getEventStoreId();
    }

    private String serialize(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.error("이벤트 객체 직렬화 실패 error : {}", e.getMessage(), e);
            throw new RuntimeException("이벤트 객체 직렬화에 실패하였습니다.");
        }
    }

    private Map<String, Object> deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("이벤트 객체 직렬화 실패 error : {}", e.getMessage(), e);
            throw new RuntimeException("이벤트 객체 직렬화에 실패하였습니다.");
        }
    }
}
