package com.klp.order.outbox.infrastructure.repository;

import com.klp.order.outbox.domain.entity.OutboxEvent;
import com.klp.order.outbox.domain.entity.OutboxStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findAllByStatus(OutboxStatus status);
}
