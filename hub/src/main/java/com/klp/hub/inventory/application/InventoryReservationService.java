package com.klp.hub.inventory.application;

import com.klp.hub.global.exception.BusinessException;
import com.klp.hub.inventory.application.dto.InventoryReservationCommand;
import com.klp.hub.inventory.domain.InventoryReservation;
import com.klp.hub.inventory.domain.repository.InventoryReservationRepository;
import com.klp.hub.inventory.exception.InventoryErrorCode;
import com.klp.hub.inventory.presentation.dto.response.InventoryReservationResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    @Transactional
    public InventoryReservationResponse reserve(InventoryReservationCommand command) {
        String idempotencyKey = command.idempotencyKey();

        if (reservationRepository.existsByIdempotencyKey(idempotencyKey)) {
            return InventoryReservationResponse.already();
        }

        List<InventoryReservation> reservations = new ArrayList<>();

        for (InventoryReservationCommand.ReservationItem item : command.items()) {
            int available = reservationRepository.getAvailableQuantity(
                item.productId(),
                item.hubId()
            );

            if (available < item.quantity()) {
                reservationRepository.deleteAll(reservations);
                throw new BusinessException(InventoryErrorCode.INSUFFICIENT_STOCK);
            }

            InventoryReservation reservation = InventoryReservation.create(
                command.orderId(),
                item.productId(),
                item.hubId(),
                item.quantity(),
                idempotencyKey,
                LocalDateTime.now().plusMinutes(15)
            );
            reservations.add(reservation);
        }

        reservationRepository.saveAll(reservations);

        return InventoryReservationResponse.success(command.orderId());
    }

    /**
     * 선점 확정 (쿠폰 확정 후 호출) - 선점 상태를 CONFIRMED로 변경
     */
    @Transactional
    public void confirm(UUID orderId) {
        List<InventoryReservation> reservations = reservationRepository.findAllByOrderId(orderId);

        if (reservations.isEmpty()) {
            log.error("선점 정보를 찾을 수 없습니다. orderId={}", orderId);
            throw new BusinessException(InventoryErrorCode.RESERVATION_NOT_FOUND);
        }

        int confirmed = reservationRepository.confirmAll(orderId);
        if (confirmed == 0) {
            log.warn("이미 처리된 선점입니다. orderId={}", orderId);
            throw new BusinessException(InventoryErrorCode.RESERVATION_ALREADY_PROCESSED);
        }

        log.info("재고 선점 확정 완료. orderId={}, confirmedCount={}", orderId, confirmed);
    }

    /**
     * 선점 해제 (결제 실패 시 호출) - 선점 상태를 RELEASED로 변경
     */
    @Transactional
    public void release(UUID orderId) {
        List<InventoryReservation> reservations = reservationRepository.findAllByOrderId(orderId);

        if (reservations.isEmpty()) {
            // 이미 해제되었거나 존재하지 않는 경우 - 멱등성 보장
            log.info("선점 정보가 없거나 이미 해제됨. orderId={}", orderId);
            return;
        }

        int released = reservationRepository.releaseAll(orderId);
        log.info("재고 선점 해제 완료. orderId={}, releasedCount={}", orderId, released);
    }
}
