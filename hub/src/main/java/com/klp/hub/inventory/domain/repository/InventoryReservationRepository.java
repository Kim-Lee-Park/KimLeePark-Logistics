package com.klp.hub.inventory.domain.repository;

import com.klp.hub.inventory.domain.InventoryReservation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryReservationRepository {

    InventoryReservation save(InventoryReservation reservation);

    List<InventoryReservation> saveAll(List<InventoryReservation> reservations);

    Optional<InventoryReservation> findByOrderIdAndProductId(UUID orderId, UUID productId);

    List<InventoryReservation> findAllByOrderId(UUID orderId);

    boolean existsByIdempotencyKey(String idempotencyKey);

    int getReservedQuantity(UUID productId, UUID hubId);

    int confirmAll(UUID orderId);

    int releaseAll(UUID orderId);

    void deleteExpiredReservations();
}
