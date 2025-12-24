package com.klp.hub.inventory.infrastructure.outbox;

import com.klp.hub.inventory.domain.outbox.InventoryOutbox;
import com.klp.hub.inventory.domain.outbox.InventoryOutboxRepository;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InventoryOutboxRepositoryImpl implements InventoryOutboxRepository {

    private final InventoryOutboxJpaRepository jpaRepository;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public InventoryOutbox save(InventoryOutbox outbox) {
        return jpaRepository.save(outbox);
    }

    @Override
    public List<InventoryOutbox> findPendingEvents(int limit) {
        return jpaRepository.findPendingEvents(limit);
    }

    @Override
    public void markAsPublished(UUID outboxId) {
        jpaRepository.markAsPublished(outboxId);
    }

    @Override
    public void markAsFailed(UUID outboxId) {
        jpaRepository.markAsFailed(outboxId);
    }

    @Override
    public void deletePublishedEvents() {
        jpaRepository.deletePublishedEvents();
    }

    @Override
    public void saveAllInBatch(List<InventoryOutbox> outboxes) {
        String sql = """
            INSERT INTO p_inventory_outbox 
            (outbox_id, order_id, event_type, payload, status, created_at, retry_count)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        jdbcTemplate.batchUpdate(sql, outboxes, 100, (ps, outbox) -> {
            ps.setObject(1, UUID.randomUUID());
            ps.setObject(2, outbox.getOrderId());
            ps.setString(3, outbox.getEventType());
            ps.setString(4, outbox.getPayload());
            ps.setString(5, "PENDING");
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(7, 0);
        });
    }
}
