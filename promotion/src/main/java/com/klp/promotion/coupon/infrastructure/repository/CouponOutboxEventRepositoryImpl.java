package com.klp.promotion.coupon.infrastructure.repository;

import com.klp.promotion.coupon.domain.entity.outbox.CouponOutboxEvent;
import com.klp.promotion.coupon.domain.repository.CouponOutboxEventRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class CouponOutboxEventRepositoryImpl implements CouponOutboxEventRepository {

    private final CouponOutboxEventJpaRepository couponOutboxEventJpaRepository;

    @Override
    public CouponOutboxEvent save(CouponOutboxEvent event) {
        return couponOutboxEventJpaRepository.save(event);
    }

    @Override
    public List<CouponOutboxEvent> findPendingEvents(int limit) {
        return couponOutboxEventJpaRepository.findPendingEvents(limit);
    }

    @Override
    @Transactional
    public void markAsPublished(UUID id) {
        couponOutboxEventJpaRepository.markAsPublished(id);
    }

    @Override
    @Transactional
    public void markAsFailed(UUID id) {
        couponOutboxEventJpaRepository.markAsFailed(id);
    }
}

