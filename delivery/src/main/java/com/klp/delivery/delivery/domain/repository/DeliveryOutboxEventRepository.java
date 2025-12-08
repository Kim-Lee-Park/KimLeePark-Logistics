package com.klp.delivery.delivery.domain.repository;

import com.klp.delivery.delivery.domain.entity.outbox.DeliveryOutboxEvent;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryOutboxEventRepository {

    DeliveryOutboxEvent save(DeliveryOutboxEvent event);

    DeliveryOutboxEvent saveAndFlush(DeliveryOutboxEvent event);

    List<DeliveryOutboxEvent> findPendingEvents();

    Optional<DeliveryOutboxEvent> findById(UUID id);

    List<DeliveryOutboxEvent> findStuckPublishingEvents();

    List<DeliveryOutboxEvent> findFailedEvents();
}

