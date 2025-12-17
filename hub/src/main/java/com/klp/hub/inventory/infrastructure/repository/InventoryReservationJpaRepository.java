package com.klp.hub.inventory.infrastructure.repository;

import com.klp.hub.inventory.domain.InventoryReservation;
import com.klp.hub.inventory.domain.repository.dto.InventoryAvailability;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryReservationJpaRepository extends JpaRepository<InventoryReservation, UUID> {


    List<InventoryReservation> findAllByOrderId(UUID orderId);

    boolean existsByIdempotencyKey(String idempotencyKey);

    @Query("""
            SELECT COALESCE(i.quantity, 0) - COALESCE(
                (SELECT SUM(r.quantity) FROM InventoryReservation r
                 WHERE r.productId = :productId AND r.hubId = :hubId AND r.status = 'RESERVED'), 0)
            FROM Inventory i
            WHERE i.productId = :productId AND i.hubId = :hubId
        """)
    int getAvailableQuantity(@Param("productId") UUID productId, @Param("hubId") UUID hubId);

    @Query("""
            SELECT new com.klp.hub.inventory.domain.repository.dto.InventoryAvailability(
                i.productId,
                i.hubId,
                COALESCE(i.quantity, 0) - COALESCE(SUM(r.quantity), 0)
            )
            FROM Inventory i
            LEFT JOIN InventoryReservation r
                ON r.productId = i.productId
               AND r.hubId = i.hubId
               AND r.status = 'RESERVED'
            WHERE i.productId IN :productIds
              AND i.hubId IN :hubIds
            GROUP BY i.productId, i.hubId, i.quantity
        """)
    List<InventoryAvailability> getAvailableQuantities(
        @Param("productIds") List<UUID> productIds,
        @Param("hubIds") List<UUID> hubIds
    );

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
