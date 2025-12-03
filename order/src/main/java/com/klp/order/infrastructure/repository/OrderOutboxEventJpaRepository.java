package com.klp.order.infrastructure.repository;

import com.klp.order.domain.entity.outbox.OrderOutboxEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderOutboxEventJpaRepository extends JpaRepository<OrderOutboxEvent, UUID> {

    @Query("SELECT o FROM OrderOutboxEvent o WHERE o.status = 'PENDING' " +
        "ORDER BY o.createdAt ASC")
    List<OrderOutboxEvent> findPendingEvents();

    @Query("SELECT o FROM OrderOutboxEvent o WHERE o.status = 'PUBLISHING' " +
        "ORDER BY o.lastRetryAt ASC")
    List<OrderOutboxEvent> findStuckPublishingEvents();

    @Query("SELECT o FROM OrderOutboxEvent o WHERE o.status = 'FAILED' " +
        "ORDER BY o.createdAt DESC")
    List<OrderOutboxEvent> findFailedEvents();
}
