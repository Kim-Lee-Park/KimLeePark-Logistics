package com.klp.order.common.event;

import java.time.LocalDateTime;

public interface DomainEvent {
    String getEventId();

    LocalDateTime getOccurredAt();
}
