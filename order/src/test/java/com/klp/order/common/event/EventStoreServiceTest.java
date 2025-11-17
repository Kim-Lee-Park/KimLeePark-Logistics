package com.klp.order.common.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.order.common.event.application.service.EventStoreService;
import com.klp.order.common.event.application.service.dto.EventStoreDto;
import com.klp.order.common.event.domain.EventStore;
import com.klp.order.common.event.domain.EventType;
import com.klp.order.common.event.infrastructure.repository.EventStoreRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventStoreServiceTest {

    @Mock
    private EventStoreRepository eventStoreRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private EventStoreService eventStoreService;

    private UUID eventStoreId = UUID.randomUUID();

    private EventType eventType = EventType.ORDER_CREATED;

    private LocalDateTime publishedAt = LocalDateTime.now();

    private record TestDomainEvent(
        String name
    ) implements DomainEvent {

        @Override
        public String getEventId() {
            return "testEventId";
        }

        @Override
        public LocalDateTime getOccurredAt() {
            return LocalDateTime.now();
        }
    }

    @Test
    @DisplayName("도메인 이벤트 목록을 조회할 수 있다")
    void findAll() throws JsonProcessingException {
        EventStore eventStore = new EventStore(
            "{\"name\":test}",
            eventType,
            publishedAt
        );
        Map<String, Object> payload = Map.of("name", "test");
        when(eventStoreRepository.findAll()).thenReturn(List.of(eventStore));
        when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(payload);

        List<EventStoreDto> result = eventStoreService.findAll();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("도메인 이벤트 객체의 Json 직렬화에 실패하면 이벤트 목록 조회시 예외가 발생한다")
    void failFindAll() throws JsonProcessingException {
        EventStore invalidEventStore = new EventStore(
            "{\"invalid\":invalid}",
            eventType,
            publishedAt
        );
        when(eventStoreRepository.findAll()).thenReturn(List.of(invalidEventStore));
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
            .thenThrow(new JsonProcessingException("역직렬화 실패") {});

        assertThrows(RuntimeException.class, () -> eventStoreService.findAll());
    }

    @Test
    @DisplayName("도메인 이벤트 객체를 생성할 수 있다")
    void createDomainEvent() throws JsonProcessingException {
        DomainEvent event = new EventStoreServiceTest.TestDomainEvent("test");
        EventStore eventStore = mock(EventStore.class);
        when(eventStoreRepository.save(any(EventStore.class)))
            .thenReturn(eventStore);
        when(eventStore.getEventStoreId()).thenReturn(eventStoreId);
        when(objectMapper.writeValueAsString(event)).thenReturn("event");

        eventStoreService.saveEvent(event, eventType, publishedAt);

        verify(eventStoreRepository, times(1)).save(any(EventStore.class));
    }

    @Test
    @DisplayName("이벤트 객체 직렬화에 실패하면 예외가 발생한다")
    void throwJsonSerializationException() throws JsonProcessingException {
        DomainEvent event = new TestDomainEvent("test");
        when(objectMapper.writeValueAsString(event)).thenThrow(new JsonProcessingException("직렬화 에러") {});

        assertThrows(RuntimeException.class, () -> eventStoreService.saveEvent(event, eventType, publishedAt));
        verify(eventStoreRepository, never()).save(any(EventStore.class));
    }
}
