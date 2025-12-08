package com.klp.hub.inventory.domain.repository;

import com.klp.hub.inventory.domain.InventoryReservation;
import java.util.List;
import java.util.UUID;

public interface InventoryReservationRepository {
    
    List<InventoryReservation> saveAll(List<InventoryReservation> reservations);

    void deleteAll(List<InventoryReservation> reservations);

    List<InventoryReservation> findAllByOrderId(UUID orderId);

    boolean existsByIdempotencyKey(String idempotencyKey);

    int getAvailableQuantity(UUID productId, UUID hubId);

    int confirmAll(UUID orderId);

    int releaseAll(UUID orderId);

    void deleteExpiredReservations();
}
