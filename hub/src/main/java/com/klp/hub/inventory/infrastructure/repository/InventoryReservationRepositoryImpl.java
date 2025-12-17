package com.klp.hub.inventory.infrastructure.repository;

import com.klp.hub.inventory.domain.InventoryReservation;
import com.klp.hub.inventory.domain.repository.InventoryReservationRepository;
import com.klp.hub.inventory.domain.repository.dto.InventoryAvailability;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InventoryReservationRepositoryImpl implements InventoryReservationRepository {

    private final InventoryReservationJpaRepository jpaRepository;
    private final JdbcTemplate jdbcTemplate;

    private static final String RESERVATION_TABLE = "p_inventory_reservation";

    @Override
    public List<InventoryReservation> saveAll(List<InventoryReservation> reservations) {
        return jpaRepository.saveAll(reservations);
    }

    @Override
    public void saveAllInBatch(List<InventoryReservation> reservations) {
        if (reservations.isEmpty()) {
            return;
        }

        String sql = String.format("""
            INSERT INTO %s
                (order_id, product_id, hub_id, idempotency_key, quantity, status, expires_at, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, RESERVATION_TABLE);

        LocalDateTime now = LocalDateTime.now();

        List<Object[]> batchArgs = reservations.stream()
            .map(reservation -> new Object[]{
                reservation.getOrderId(),
                reservation.getProductId(),
                reservation.getHubId(),
                reservation.getIdempotencyKey(),
                reservation.getQuantity(),
                reservation.getStatus().name(),
                reservation.getExpiresAt(),
                now,
                now
            })
            .toList();

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    @Override
    public void deleteAll(List<InventoryReservation> reservations) {
        jpaRepository.deleteAll(reservations);
    }

    @Override
    public List<InventoryReservation> findAllByOrderId(UUID orderId) {
        return jpaRepository.findAllByOrderId(orderId);
    }

    @Override
    public boolean existsByIdempotencyKey(String idempotencyKey) {
        return jpaRepository.existsByIdempotencyKey(idempotencyKey);
    }

    @Override
    public int getAvailableQuantity(UUID productId, UUID hubId) {
        return jpaRepository.getAvailableQuantity(productId, hubId);
    }

    @Override
    public List<InventoryAvailability> getAvailableQuantities(
        List<UUID> productIds,
        List<UUID> hubIds
    ) {
        return jpaRepository.getAvailableQuantities(productIds, hubIds);
    }

    @Override
    public int confirmAll(UUID orderId) {
        return jpaRepository.confirmAllByOrderId(orderId);
    }

    @Override
    public int releaseAll(UUID orderId) {
        return jpaRepository.releaseAllByOrderId(orderId);
    }

    @Override
    public void deleteExpiredReservations() {
        jpaRepository.deleteExpiredReservations(LocalDateTime.now());
    }
}
