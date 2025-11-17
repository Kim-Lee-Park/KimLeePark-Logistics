package com.klp.order.common.event.domain;

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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "p_event_store", schema = "event_schema")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventStore {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "event_store_id", nullable = false)
    private UUID eventStoreId;

    @Comment("이벤트 타입")
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Comment("이벤트 객체")
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String payload;

    @Comment("발행 시간")
    @Column(name = "published_at", nullable = false)
    private LocalDateTime publishedAt;

    @Comment("생성 시간")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public EventStore(String payload, EventType eventType, LocalDateTime publishedAt) {
        if (eventType == null) {
            throw new IllegalArgumentException("이벤트 타입은 필수값입니다.");
        }

        if (publishedAt == null) {
            throw new IllegalArgumentException("발행 시간은 필수값입니다.");
        }

        if (payload == null) {
            throw new IllegalArgumentException("Payload 는 필수값입니다.");
        }

        this.payload = payload;
        this.eventType = eventType;
        this.publishedAt = publishedAt;
        this.createdAt = LocalDateTime.now();
    }
}
