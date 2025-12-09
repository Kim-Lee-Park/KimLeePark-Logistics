package com.klp.promotion.coupon.infrastructure.repository;

import com.klp.promotion.coupon.domain.entity.outbox.CouponOutboxEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouponOutboxEventJpaRepository extends JpaRepository<CouponOutboxEvent, UUID> {

    @Query("SELECT o FROM CouponOutboxEvent o WHERE o.status = 'PENDING' ORDER BY o.createdAt ASC LIMIT :limit")
    List<CouponOutboxEvent> findPendingEvents(
        @Param("limit") int limit
    );

    @Modifying
    @Query("UPDATE CouponOutboxEvent o SET o.status = 'PUBLISHED', o.publishedAt = CURRENT_TIMESTAMP WHERE o.id = :id")
    void markAsPublished(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE CouponOutboxEvent o SET o.status = 'FAILED', o.retryCount = o.retryCount + 1 WHERE o.id = :id")
    void markAsFailed(@Param("id") UUID id);
}

