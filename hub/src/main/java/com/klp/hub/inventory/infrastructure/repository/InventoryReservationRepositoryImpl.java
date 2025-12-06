package com.klp.hub.inventory.infrastructure.repository;

import com.klp.hub.inventory.domain.InventoryReservation;
import com.klp.hub.inventory.domain.repository.InventoryReservationRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InventoryReservationRepositoryImpl implements InventoryReservationRepository {

    private final InventoryReservationJpaRepository jpaRepository;

    @Override
    public InventoryReservation save(InventoryReservation reservation) {
        return jpaRepository.save(reservation);
    }

    @Override
    public List<InventoryReservation> saveAll(List<InventoryReservation> reservations) {
        return jpaRepository.saveAll(reservations);
    }

    @Override
    public Optional<InventoryReservation> findByOrderIdAndProductId(UUID orderId, UUID productId) {
        return jpaRepository.findByOrderIdAndProductId(orderId, productId);
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
    public int getReservedQuantity(UUID productId, UUID hubId) {
        return jpaRepository.getReservedQuantity(productId, hubId);
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
