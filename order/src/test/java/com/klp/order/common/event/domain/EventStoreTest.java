package com.klp.order.common.event.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EventStoreTest {

    private String payload = "payload";

    private EventType eventType = EventType.ORDER_CREATED;

    private LocalDateTime publishedAt = LocalDateTime.now();

    @Test
    @DisplayName("이벤트 타입이 Null 이라면 예외가 발생한다")
    void eventTypeIsNull() {
        EventType eventType = null;
        assertThrows(IllegalArgumentException.class, () -> new EventStore(payload, eventType, publishedAt));
    }

    @Test
    @DisplayName("발행 시간이 Null 이라면 예외가 발생한다")
    void publishedAtIsNull() {
        LocalDateTime publishedAt = null;
        assertThrows(IllegalArgumentException.class, () -> new EventStore(payload, eventType, publishedAt));
    }

    @Test
    @DisplayName("payload 객체가 Null 이라면 예외가 발생한다")
    void payloadIsNull() {
        String payload = null;
        assertThrows(IllegalArgumentException.class, () -> new EventStore(payload, eventType, publishedAt));
    }
}
