package com.klp.promotion.coupon.domain.entity.outbox;

import com.klp.promotion.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "p_coupon_outbox_events", schema = "promotion_schema")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponOutboxEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Comment("아웃박스 이벤트 ID")
    private UUID id;

    @Comment("주문 ID")
    @Column(nullable = false)
    private UUID orderId;

    @Comment("이벤트 타입")
    @Column(nullable = false)
    private String eventType;

    @Comment("이벤트 페이로드")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Comment("이벤트 상태")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponOutboxStatus status;

    @Comment("재시도 횟수")
    @Column(nullable = false)
    private Integer retryCount = 0;

    @Comment("발행 시간")
    private LocalDateTime publishedAt;

    @Comment("마지막 재시도 시간")
    private LocalDateTime lastRetryAt;

    public static CouponOutboxEvent create(UUID orderId,
        String eventType, String payload) {
        CouponOutboxEvent event = new CouponOutboxEvent();
        event.orderId = orderId;
        event.eventType = eventType;
        event.payload = payload;
        event.status = CouponOutboxStatus.PENDING;
        return event;
    }

    private static final int MAX_RETRY_COUNT = 20;           // 최대 20회
    private static final long INITIAL_BACKOFF_MILLIS = 1000L;  // 초기 1초
    private static final long MAX_BACKOFF_MILLIS = 300000L;    // 최대 5분

    public void markAsPublishing() {
        this.status = CouponOutboxStatus.PUBLISHING;
        this.lastRetryAt = LocalDateTime.now();
        this.retryCount++;
    }

    public void markAsPublished() {
        this.status = CouponOutboxStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        this.retryCount++;
        this.lastRetryAt = LocalDateTime.now();

        // 20회 초과 시 FAILED 처리
        if (this.retryCount >= MAX_RETRY_COUNT) {
            this.status = CouponOutboxStatus.FAILED;
        }
    }

    public boolean canRetry() {
        return this.status != CouponOutboxStatus.PUBLISHED
            && this.status != CouponOutboxStatus.FAILED;
    }

    /**
     * AWS/Google Cloud 권장 전략 1초 → 2초 → 4초 → 8초 → ... → 최대 5분
     */
    // 1초 2초 ... 최대 5분 -> AWS, GOOGLE 방식
    public long getBackoffMillis() {
        // Exponential: 1초 * 2^(retryCount-1)
        long exponentialBackoff = INITIAL_BACKOFF_MILLIS * (1L << (this.retryCount - 1));

        // 최대값 제한
        long cappedBackoff = Math.min(exponentialBackoff, MAX_BACKOFF_MILLIS);

        // Jitter 추가 (0~20% 랜덤 변동)
        double jitterFactor = 0.8 + (Math.random() * 0.4); // 0.8 ~ 1.2
        return (long) (cappedBackoff * jitterFactor);
    }

    public boolean shouldRetryNow() {
        if (!canRetry()) {
            return false;
        }

        // 첫 시도이거나 backoff 시간이 지났으면 재시도
        if (this.lastRetryAt == null) {
            return true;
        }

        long elapsed = java.time.Duration.between(this.lastRetryAt, LocalDateTime.now())
            .toMillis();
        return elapsed >= getBackoffMillis();
    }

    public void resetToPending() {
        if (this.status == CouponOutboxStatus.PUBLISHING) {
            this.status = CouponOutboxStatus.PENDING;
        }
    }

}

