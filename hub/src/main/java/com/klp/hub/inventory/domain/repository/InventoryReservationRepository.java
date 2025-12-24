package com.klp.hub.inventory.domain.repository;

import com.klp.hub.inventory.domain.InventoryReservation;
import com.klp.hub.inventory.domain.repository.dto.InventoryAvailability;
import java.util.List;
import java.util.UUID;

public interface InventoryReservationRepository {

    List<InventoryReservation> saveAll(List<InventoryReservation> reservations);

    void saveAllInBatch(List<InventoryReservation> reservations);

    void deleteAll(List<InventoryReservation> reservations);

    List<InventoryReservation> findAllByOrderId(UUID orderId);

    boolean existsByIdempotencyKey(String idempotencyKey);

    int getAvailableQuantity(UUID productId, UUID hubId);

    List<InventoryAvailability> getAvailableQuantities(
        List<UUID> productIds,
        List<UUID> hubIds
    );

    int confirmAll(UUID orderId);

    int releaseAll(UUID orderId);

    void deleteExpiredReservations();
}
