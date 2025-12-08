package com.klp.delivery.delivery.infrastructure.repository;

import com.klp.delivery.delivery.domain.entity.outbox.DeliveryOutboxEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DeliveryOutboxEventJpaRepository extends JpaRepository<DeliveryOutboxEvent, UUID> {

    @Query("SELECT o FROM DeliveryOutboxEvent o WHERE o.status = 'PENDING' " +
        "ORDER BY o.createdAt ASC")
    List<DeliveryOutboxEvent> findPendingEvents();

    @Query("SELECT o FROM DeliveryOutboxEvent o WHERE o.status = 'PUBLISHING' " +
        "ORDER BY o.lastRetryAt ASC")
    List<DeliveryOutboxEvent> findStuckPublishingEvents();

    @Query("SELECT o FROM DeliveryOutboxEvent o WHERE o.status = 'FAILED' " +
        "ORDER BY o.createdAt DESC")
    List<DeliveryOutboxEvent> findFailedEvents();
}

