package com.klp.hub.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Table(
    name = "p_inventory_idempotency",
    schema = "hub_schema",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_inventory_idempotency_key", columnNames = "idempotency_key"
        )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryIdempotency {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "inventory_idempotency_id", nullable = false)
    private UUID id;

    @Comment("멱등키")
    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Comment("요청 상태")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InventoryIdempotencyStatus status;

    public InventoryIdempotency(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("멱등키는 필수값 입니다.");
        }
        this.idempotencyKey = idempotencyKey;
        this.status = InventoryIdempotencyStatus.IN_PROGRESS;
    }

    public void success() {
        this.status = InventoryIdempotencyStatus.SUCCESS;
    }

    public boolean isUsed() {
        return status.isUsed();
    }
}
