package com.klp.hub.inventory.application;

import com.klp.common.exception.BusinessException;
import com.klp.hub.inventory.application.dto.InventoryReplenishCommand;
import com.klp.hub.inventory.domain.event.OrderCreatedEvent;
import com.klp.hub.inventory.exception.InventoryErrorCode;
import com.klp.hub.inventory.infrastructure.lock.DistributedLockManager;
import com.klp.hub.inventory.presentation.dto.InventoryDeductResponse;
import com.klp.hub.inventory.presentation.dto.InventoryReplenishResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class InventoryFacade {

    private final InventoryService inventoryService;

    private final DistributedLockManager lockManager;

    public InventoryDeductResponse deduct(OrderCreatedEvent event) {
        String idempotencyKey = event.idempotencyKey();

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
            InventoryReplenishResponse response = inventoryService.replenish(command);
            return response;
        } finally {
            unLock(idempotencyKey);
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
