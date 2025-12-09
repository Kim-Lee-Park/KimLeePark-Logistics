package com.klp.promotion.coupon.domain.repository;

import com.klp.promotion.coupon.domain.entity.outbox.CouponOutboxEvent;
import java.util.List;
import java.util.UUID;

public interface CouponOutboxEventRepository {

    CouponOutboxEvent save(CouponOutboxEvent event);

    List<CouponOutboxEvent> findPendingEvents(int limit);

    void markAsPublished(UUID id);

    void markAsFailed(UUID id);
}

