package com.klp.order.domain.repository;

import com.klp.order.domain.entity.outbox.OrderOutboxEvent;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderOutboxEventRepository {

    OrderOutboxEvent save(OrderOutboxEvent event);

    List<OrderOutboxEvent> findPendingEvents();

    Optional<OrderOutboxEvent> findById(UUID id);

    List<OrderOutboxEvent> findStuckPublishingEvents();

    List<OrderOutboxEvent> findFailedEvents();
}
