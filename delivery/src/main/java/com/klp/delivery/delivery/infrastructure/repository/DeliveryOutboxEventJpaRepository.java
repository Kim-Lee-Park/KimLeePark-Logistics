package com.klp.delivery.delivery.infrastructure.repository;

import com.klp.delivery.delivery.domain.entity.outbox.DeliveryOutboxEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeliveryOutboxEventJpaRepository extends JpaRepository<DeliveryOutboxEvent, UUID> {

    @Query("SELECT o FROM DeliveryOutboxEvent o WHERE o.status = 'PENDING' ORDER BY o.createdAt ASC LIMIT :limit")
    List<DeliveryOutboxEvent> findPendingEvents(
        @Param("limit") int limit
    );

    @Modifying
    @Query("UPDATE DeliveryOutboxEvent o SET o.status = 'PUBLISHED', o.publishedAt = CURRENT_TIMESTAMP WHERE o.id = :id")
    void markAsPublished(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE DeliveryOutboxEvent o SET o.status = 'FAILED', o.retryCount = o.retryCount + 1 WHERE o.id = :id")
    void markAsFailed(@Param("id") UUID id);

    @Query("SELECT o FROM DeliveryOutboxEvent o WHERE o.deliveryId = :deliveryId AND o.eventType = :eventType AND o.status = 'PENDING' ORDER BY o.createdAt ASC")
    List<DeliveryOutboxEvent> findByDeliveryIdAndEventType(
        @Param("deliveryId") UUID deliveryId,
        @Param("eventType") String eventType
    );
}

