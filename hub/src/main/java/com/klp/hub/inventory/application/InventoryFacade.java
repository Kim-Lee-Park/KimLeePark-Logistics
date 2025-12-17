package com.klp.hub.inventory.application;

import com.klp.hub.global.exception.BusinessException;
import com.klp.hub.inventory.application.dto.InventoryReplenishCommand;
import com.klp.hub.inventory.application.dto.InventoryReservationCommand;
import com.klp.hub.inventory.domain.event.CouponCancelledEvent;
import com.klp.hub.inventory.domain.event.CouponUsedEvent;
import com.klp.hub.inventory.exception.InventoryErrorCode;
import com.klp.hub.inventory.infrastructure.lock.DistributedLockManager;
import com.klp.hub.inventory.presentation.dto.response.InventoryDeductResponse;
import com.klp.hub.inventory.presentation.dto.response.InventoryReplenishResponse;
import com.klp.hub.inventory.presentation.dto.response.InventoryReservationResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class InventoryFacade {

    private final InventoryService inventoryService;
    private final InventoryReservationService inventoryReservationService;
    private final DistributedLockManager lockManager;

    public InventoryDeductResponse deduct(CouponUsedEvent event) {
        String idempotencyKey = event.inventoryIdempotencyKey();

        lock(idempotencyKey);
        try {
            return inventoryService.deduct(event);
        } finally {
            unLock(idempotencyKey);
        }
    }

    public InventoryReplenishResponse replenish(InventoryReplenishCommand command) {
        String idempotencyKey = command.idempotencyKey();

        lock(idempotencyKey);
        try {
            return inventoryService.replenish(command);
        } finally {
            unLock(idempotencyKey);
        }
    }

    public InventoryReservationResponse reserve(InventoryReservationCommand command) {
        String lockKey = "inventory:reserve:" + command.orderId();

        lock(lockKey);
        try {
            return inventoryReservationService.reserve(command);
        } finally {
            unLock(lockKey);
        }
    }

    /**
     * 선점 확정 (쿠폰 확정 후 호출)
     */
    public void confirm(UUID orderId) {
        String lockKey = "inventory:confirm:" + orderId;

        lock(lockKey);
        try {
            inventoryReservationService.confirm(orderId);
        } finally {
            unLock(lockKey);
        }
    }

    /**
     * 선점 해제 (결제, 쿠폰사용 실패 시 호출)
     */
    public void release(UUID orderId) {
        String lockKey = "inventory:release:" + orderId;

        lock(lockKey);
        try {
            inventoryReservationService.release(orderId);
        } finally {
            unLock(lockKey);
        }
    }

    /**
     * 재고 복원 (결제 취소 시 호출)
     */
    public void replenishFromCancellation(CouponCancelledEvent event) {
        String lockKey = "inventory:replenish:" + event.orderId();

        lock(lockKey);
        try {
            inventoryService.replenishFromCancellation(event);
        } finally {
            unLock(lockKey);
        }
    }

    private void lock(String idempotencyKey) {
        boolean locked = lockManager.tryLock(idempotencyKey);
        if (!locked) {
            log.warn("이미 해당 멱등키로 재고 처리중 [Redis 락 획득 실패] idempotencyKey = {}", idempotencyKey);
            throw new BusinessException(InventoryErrorCode.IDEMPOTENCY_ALREADY_PROCESSING);
        }
    }

    private void unLock(String idempotencyKey) {
        log.info("Redis 락 해제 idempotencyKey = {}", idempotencyKey);
        lockManager.releaseLock(idempotencyKey);
    }
}
