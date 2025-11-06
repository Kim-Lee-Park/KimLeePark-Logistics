package com.klp.hub.inventory.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.util.UUID;

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
        if (idempotencyKey == null) {
            throw new IllegalArgumentException("멱등키는 필수값 입니다.");
        }
        this.idempotencyKey = idempotencyKey;
    }
}
