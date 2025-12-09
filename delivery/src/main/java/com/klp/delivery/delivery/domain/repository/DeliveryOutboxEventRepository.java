package com.klp.delivery.delivery.domain.repository;

import com.klp.delivery.delivery.domain.entity.outbox.DeliveryOutboxEvent;
import java.util.List;
import java.util.UUID;

public interface DeliveryOutboxEventRepository {

    DeliveryOutboxEvent save(DeliveryOutboxEvent event);

    List<DeliveryOutboxEvent> findPendingEvents(int limit);

    void markAsPublished(UUID id);

    void markAsFailed(UUID id);

    List<DeliveryOutboxEvent> findByDeliveryIdAndEventType(UUID deliveryId, String eventType);
}

