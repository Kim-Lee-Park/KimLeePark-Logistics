package com.klp.delivery.delivery.domain.entity;

import com.klp.delivery.common.entity.BaseEntity;
import com.klp.delivery.common.enums.IdempotencyStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@Table(name = "p_delivery_idempotency", schema = "delivery_schema")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IdempotencyKey extends BaseEntity {

    @Id
    @Column(name = "idempotency_key", nullable = false)
    @Comment("멱등키")
    private String idempotencyKey;

    @Column(name = "order_id", nullable = false)
    @Comment("주문ID")
    private UUID orderId;

    @Comment("진행상태")
    @Enumerated(EnumType.STRING)
    IdempotencyStatus status;

    public IdempotencyKey(String idempotencyKey, UUID orderId, IdempotencyStatus status) {
        this.idempotencyKey = idempotencyKey;
        this.orderId = orderId;
        this.status = status;
    }

    public static IdempotencyKey create(String key, UUID orderId, IdempotencyStatus status) {
        return new IdempotencyKey(key, orderId, status);
    }


    public void updateStatus(IdempotencyStatus status) {
        this.status = status;
    }


}
