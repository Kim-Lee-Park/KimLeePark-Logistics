package com.klp.order.domain.entity.outbox;

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
public class OrderOutboxEvent {

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

    public void markAsPublished() {
        this.status = OrderOutboxStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        this.status = OrderOutboxStatus.FAILED;
        this.retryCount++;
    }
}
