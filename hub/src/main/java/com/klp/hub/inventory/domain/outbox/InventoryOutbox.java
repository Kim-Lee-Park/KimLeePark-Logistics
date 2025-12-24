package com.klp.hub.inventory.domain.outbox;

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
@Table(name = "p_inventory_outbox")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "outbox_id", nullable = false)
    private UUID id;

    @Comment("주문 ID")
    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Comment("이벤트 타입")
    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Comment("이벤트 내용(JSON 형태)")
    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Comment("발행 상태")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OutboxStatus status;

    @Comment("생성 시간")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Comment("발행 시간")
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Comment("재시도 횟수")
    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    public InventoryOutbox(UUID orderId, String eventType, String payload) {
        this.orderId = orderId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.retryCount = 0;
    }

    public static InventoryOutbox create(UUID orderId, String eventType, String payload) {
        return new InventoryOutbox(orderId, eventType, payload);
    }

    public void publish() {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    public void fail() {
        this.status = OutboxStatus.FAILED;
        this.retryCount++;
    }

    public void incrementRetry() {
        this.retryCount++;
    }

    public boolean isRetryable(int maxRetry) {
        return this.retryCount < maxRetry;
    }
}
