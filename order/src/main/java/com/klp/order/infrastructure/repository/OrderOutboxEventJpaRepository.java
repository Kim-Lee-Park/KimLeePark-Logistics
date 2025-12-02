package com.klp.order.infrastructure.repository;

import com.klp.order.domain.entity.outbox.OrderOutboxEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderOutboxEventJpaRepository extends JpaRepository<OrderOutboxEvent, UUID> {

    @Query("SELECT o FROM OutboxEvent o WHERE o.status = 'PENDING' " +
        "AND o.retryCount < 3 ORDER BY o.createdAt ASC")
    List<OrderOutboxEvent> findPendingEvents();

}
