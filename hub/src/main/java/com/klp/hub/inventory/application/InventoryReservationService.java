package com.klp.hub.inventory.application;

import com.klp.hub.global.exception.BusinessException;
import com.klp.hub.inventory.application.dto.InventoryReservationCommand;
import com.klp.hub.inventory.application.dto.InventoryReservationCommand.ReservationItem;
import com.klp.hub.inventory.domain.InventoryReservation;
import com.klp.hub.inventory.domain.repository.InventoryRepository;
import com.klp.hub.inventory.domain.repository.InventoryReservationRepository;
import com.klp.hub.inventory.domain.repository.dto.InventoryAvailability;
import com.klp.hub.inventory.domain.repository.dto.InventoryDeduct;
import com.klp.hub.inventory.exception.InventoryErrorCode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryReservationService {

    private final InventoryReservationRepository reservationRepository;
    private final InventoryRepository inventoryRepository;

    @Transactional
    public void reserve(UUID orderId, String idempotencyKey, List<ReservationItem> items) {
        List<UUID> productIds = items.stream()
            .map(InventoryReservationCommand.ReservationItem::productId)
            .distinct()
            .toList();

        List<UUID> hubIds = items.stream()
            .map(InventoryReservationCommand.ReservationItem::hubId)
            .distinct()
            .toList();

        List<InventoryAvailability> availabilityList =
            reservationRepository.getAvailableQuantities(productIds, hubIds);

        Map<String, Integer> availabilityMap = new HashMap<>();
        for (InventoryAvailability availability : availabilityList) {
            String key = availability.productId() + ":" + availability.hubId();
            Integer available = availability.availableQuantity() == null
                ? 0
                : availability.availableQuantity().intValue();
            availabilityMap.put(key, available);
        }

        List<InventoryReservation> reservations = new ArrayList<>();

        for (InventoryReservationCommand.ReservationItem item : items) {
            String key = item.productId() + ":" + item.hubId();
            int available = availabilityMap.getOrDefault(key, 0);

            if (available < item.quantity()) {
                log.error("재고 부족: productId={}, hubId={}, available={}, requested={}",
                    item.productId(), item.hubId(), available, item.quantity());
                throw new BusinessException(InventoryErrorCode.INSUFFICIENT_STOCK);
            }

            InventoryReservation reservation = InventoryReservation.create(
                orderId,
                item.productId(),
                item.hubId(),
                item.quantity(),
                idempotencyKey,
                LocalDateTime.now().plusMinutes(15)
            );
            reservations.add(reservation);
        }

        reservationRepository.saveAllInBatch(reservations);
    }

    /**
     * 선점 확정 (쿠폰 확정 후 호출) - 선점 상태를 CONFIRMED로 변경하고 실제 재고 차감
     */
    @Transactional
    public void confirm(UUID orderId) {
        List<InventoryReservation> reservations = reservationRepository.findAllByOrderId(orderId);

        if (reservations.isEmpty()) {
            log.error("선점 정보를 찾을 수 없습니다. orderId={}", orderId);
            throw new BusinessException(InventoryErrorCode.RESERVATION_NOT_FOUND);
        }

        List<InventoryReservation> reservedItems = reservations.stream()
            .filter(InventoryReservation::isReserved)
            .toList();

        if (reservedItems.isEmpty()) {
            log.warn("이미 처리된 선점입니다. orderId={}", orderId);
            throw new BusinessException(InventoryErrorCode.RESERVATION_ALREADY_PROCESSED);
        }

        List<InventoryDeduct> deductPlans = reservedItems.stream()
            .map(r -> new InventoryDeduct(r.getProductId(), r.getHubId(), r.getQuantity()))
            .toList();

        int deducted = inventoryRepository.deductAll(deductPlans);
        if (deducted != deductPlans.size()) {
            log.error("재고 차감 실패. orderId={}", orderId);
            throw new BusinessException(InventoryErrorCode.INSUFFICIENT_STOCK);
        }

        int confirmed = reservationRepository.confirmAll(orderId);
        log.info("재고 선점 확정 완료. orderId={}, confirmedCount={}, deductedCount={}",
            orderId, confirmed, deducted);
    }

    /**
     * 선점 해제 (결제 실패 시 호출) - 선점 상태를 RELEASED로 변경
     */
    @Transactional
    public void release(UUID orderId) {
        List<InventoryReservation> reservations = reservationRepository.findAllByOrderId(orderId);

        if (reservations.isEmpty()) {
            log.info("선점 정보가 없거나 이미 해제됨. orderId={}", orderId);
            return;
        }

        int released = reservationRepository.releaseAll(orderId);
        log.info("재고 선점 해제 완료. orderId={}, releasedCount={}", orderId, released);
    }

    @Transactional(readOnly = true)
    public boolean existsByIdempotencyKey(String idempotencyKey) {
        return reservationRepository.existsByIdempotencyKey(idempotencyKey);
    }

    @Transactional(readOnly = true)
    public List<InventoryReservation> findReservationsByOrderId(UUID orderId) {
        return reservationRepository.findAllByOrderId(orderId);
    }
}
