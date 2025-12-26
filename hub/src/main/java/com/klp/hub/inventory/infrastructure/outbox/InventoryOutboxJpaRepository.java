package com.klp.hub.inventory.infrastructure.outbox;

import com.klp.hub.inventory.domain.outbox.InventoryOutbox;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryOutboxJpaRepository extends JpaRepository<InventoryOutbox, UUID> {

    @Query("""
            SELECT o FROM InventoryOutbox o
            WHERE o.status = 'PENDING'
            ORDER BY o.createdAt ASC
            LIMIT :limit
        """)
    List<InventoryOutbox> findPendingEvents(@Param("limit") int limit);

    @Modifying
    @Query("""
            UPDATE InventoryOutbox o
            SET o.status = 'PUBLISHED', o.publishedAt = CURRENT_TIMESTAMP
            WHERE o.id = :outboxId
        """)
    void markAsPublished(@Param("outboxId") UUID outboxId);

    @Modifying
    @Query("""
            UPDATE InventoryOutbox o
            SET o.status = 'FAILED', o.retryCount = o.retryCount + 1
            WHERE o.id = :outboxId
        """)
    void markAsFailed(@Param("outboxId") UUID outboxId);

    @Modifying
    @Query("DELETE FROM InventoryOutbox o WHERE o.status = 'PUBLISHED'")
    void deletePublishedEvents();

    @Modifying
    @Query("""
            UPDATE InventoryOutbox o
            SET o.status = 'PUBLISHED', o.publishedAt = CURRENT_TIMESTAMP
            WHERE o.id IN :outboxIds
        """)
    void markAsPublishedBatch(@Param("outboxIds") List<UUID> outboxIds);

    @Modifying
    @Query("""
            UPDATE InventoryOutbox o
            SET o.status = 'FAILED', o.retryCount = o.retryCount + 1
            WHERE o.id IN :outboxIds
        """)
    void markAsFailedBatch(@Param("outboxIds") List<UUID> outboxIds);

    @Query("""
            SELECT o FROM InventoryOutbox o
            WHERE o.status = 'PENDING'
            AND o.eventType = 'InventoryDbSyncEvent'
        """)
    List<InventoryOutbox> findPendingDbSyncEvents();
}
