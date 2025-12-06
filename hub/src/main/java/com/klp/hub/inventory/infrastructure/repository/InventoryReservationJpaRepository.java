package com.klp.hub.inventory.infrastructure.repository;

import com.klp.hub.inventory.domain.InventoryReservation;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryReservationJpaRepository extends JpaRepository<InventoryReservation, UUID> {

    Optional<InventoryReservation> findByOrderIdAndProductId(UUID orderId, UUID productId);

    List<InventoryReservation> findAllByOrderId(UUID orderId);

    boolean existsByIdempotencyKey(String idempotencyKey);

    @Query("""
        SELECT COALESCE(SUM(r.quantity), 0)
        FROM InventoryReservation r
        WHERE r.productId = :productId
          AND r.hubId = :hubId
          AND r.status = 'RESERVED'
    """)
    int getReservedQuantity(@Param("productId") UUID productId, @Param("hubId") UUID hubId);

    @Modifying
    @Query("""
        UPDATE InventoryReservation r
        SET r.status = 'CONFIRMED'
        WHERE r.orderId = :orderId AND r.status = 'RESERVED'
    """)
    int confirmAllByOrderId(@Param("orderId") UUID orderId);

    @Modifying
    @Query("""
        UPDATE InventoryReservation r
        SET r.status = 'RELEASED'
        WHERE r.orderId = :orderId AND r.status = 'RESERVED'
    """)
    int releaseAllByOrderId(@Param("orderId") UUID orderId);

    @Modifying
    @Query("""
        DELETE FROM InventoryReservation r
        WHERE r.status = 'RESERVED' AND r.expiresAt < :now
    """)
    void deleteExpiredReservations(@Param("now") LocalDateTime now);
}
