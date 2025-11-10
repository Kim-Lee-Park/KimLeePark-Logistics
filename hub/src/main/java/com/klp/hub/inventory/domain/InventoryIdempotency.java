package com.klp.hub.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
    @Column(name = "invevntory_idempotency_id", nullable = false)
    private UUID id;

    @Comment("멱등키")
    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    public InventoryIdempotency(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("멱등키는 필수값 입니다.");
        }
        this.idempotencyKey = idempotencyKey;
    }
}
