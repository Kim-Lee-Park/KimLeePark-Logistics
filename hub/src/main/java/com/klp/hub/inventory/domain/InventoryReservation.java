package com.klp.hub.inventory.domain;

import com.klp.hub.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "p_inventory_reservation",
    indexes = {
        @Index(name = "idx_inv_reservation_product_hub", columnList = "product_id, hub_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_inv_reservation_idempotency",
            columnNames = "idempotency_key"
        )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryReservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false, name = "product_id")
    private UUID productId;

    @Column(nullable = false, name = "hub_id")
    private UUID hubId;

    @Column(nullable = false)
    private String idempotencyKey;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryReservationStatus status = InventoryReservationStatus.RELEASED;

    private LocalDateTime expiresAt;

    public static InventoryReservation create(
        UUID orderId,
        UUID productId,
        UUID hubId,
        Integer quantity,
        String idempotencyKey,
        LocalDateTime expiresAt
    ) {
        InventoryReservation reservation = new InventoryReservation();
        reservation.orderId = orderId;
        reservation.productId = productId;
        reservation.hubId = hubId;
        reservation.quantity = quantity;
        reservation.idempotencyKey = idempotencyKey;
        reservation.status = InventoryReservationStatus.RESERVED;
        reservation.expiresAt = expiresAt;
        return reservation;
    }

    public boolean isReserved() {
        return this.status == InventoryReservationStatus.RESERVED;
    }
}
