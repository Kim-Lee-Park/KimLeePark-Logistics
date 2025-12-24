package com.klp.payment.payment.infrastructure.outbox;

import com.klp.payment.payment.domain.outbox.PaymentOutbox;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxJpaRepository extends JpaRepository<PaymentOutbox, UUID> {

    @Query("SELECT o FROM PaymentOutbox o WHERE o.status = 'PENDING' ORDER BY o.createdAt ASC LIMIT :limit")
    List<PaymentOutbox> findPendingEvents(
        @Param("limit") int limit
    );

    @Modifying
    @Query("UPDATE PaymentOutbox o SET o.status = 'PUBLISHED', o.publishedAt = CURRENT_TIMESTAMP WHERE o.paymentOutboxId = :paymentOutboxId")
    void markAsPublished(UUID paymentOutboxId);

    @Modifying
    @Query("UPDATE PaymentOutbox o SET o.status = 'FAILED', o.retryCount = o.retryCount + 1 WHERE o.paymentOutboxId = :paymentOutboxId")
    void markAsFailed(UUID paymentOutboxId);

}
