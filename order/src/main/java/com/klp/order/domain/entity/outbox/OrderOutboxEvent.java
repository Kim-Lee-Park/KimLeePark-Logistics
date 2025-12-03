package com.klp.order.domain.entity.outbox;

import com.klp.order.common.BaseEntity;
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

@Entity
@Table(name = "p_order_outbox_events", schema = "order_schema")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderOutboxEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String aggregateType;   // "ORDER" -> 우선은 블로그 탐색 결과 대부분 이런 형식 사용

    @Column(nullable = false)
    private UUID aggregateId;   // orderId

    @Column(nullable = false)
    private String eventType;   // "ORDER_CREATED, ORDER_CANCELLED"

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload; //JSON

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderOutboxStatus status;   //PENDING,PUBLISHED,FAILED

    @Column(nullable = false)
    private Integer retryCount = 0;

    private LocalDateTime publishedAt;

    private LocalDateTime lastRetryAt;

    public static OrderOutboxEvent create(String aggregateType, UUID aggregateId,
        String eventType, String payload) {
        OrderOutboxEvent event = new OrderOutboxEvent();
        event.aggregateType = aggregateType;
        event.aggregateId = aggregateId;
        event.eventType = eventType;
        event.payload = payload;
        event.status = OrderOutboxStatus.PENDING;
        return event;
    }

    private static final int MAX_RETRY_COUNT = 100;
    private static final int MIDDLE_RETRY_COUNT = 50;
    private static final long MAX_BACKOFF_MILLIS = 3600000L; // 1시간

    public void markAsPublishing() {
        this.status = OrderOutboxStatus.PUBLISHING;
        this.lastRetryAt = LocalDateTime.now();
        this.retryCount++;
    }

    public void markAsPublished() {
        this.status = OrderOutboxStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        this.retryCount++;
        this.lastRetryAt = LocalDateTime.now();

        if (this.retryCount >= MAX_RETRY_COUNT) {
            this.status = OrderOutboxStatus.FAILED;
        }
    }

    public boolean canRetry() {
        return this.status != OrderOutboxStatus.PUBLISHED
            && this.status != OrderOutboxStatus.FAILED;
    }

    public long getBackoffMillis() {
        // 50회 이상부터는 1시간 고정
        if (this.retryCount >= MIDDLE_RETRY_COUNT) {
            return MAX_BACKOFF_MILLIS;
        }
        // 1초 → 2초 → 4초 → 8초 → ... → 최대 1시간
        return Math.min(1000L * (1L << this.retryCount), MAX_BACKOFF_MILLIS);
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
        if (this.status == OrderOutboxStatus.PUBLISHING) {
            this.status = OrderOutboxStatus.PENDING;
        }
    }

}
