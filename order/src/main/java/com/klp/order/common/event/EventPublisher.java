package com.klp.order.common.event;

public interface EventPublisher {
    void publish(DomainEvent event);
}
