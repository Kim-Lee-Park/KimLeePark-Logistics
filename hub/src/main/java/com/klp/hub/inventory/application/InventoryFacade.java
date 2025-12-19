package com.klp.hub.inventory.application;

import static com.klp.hub.inventory.infrastructure.cache.InventoryCacheServiceImpl.NEED_DB_FALLBACK;
import static com.klp.hub.inventory.infrastructure.cache.InventoryCacheServiceImpl.RESULT_INSUFFICIENT_STOCK;

import com.klp.hub.global.exception.BusinessException;
import com.klp.hub.inventory.application.dto.InventoryReplenishCommand;
import com.klp.hub.inventory.application.dto.InventoryReservationCommand;
import com.klp.hub.inventory.application.dto.InventoryReservationCommand.ReservationItem;
import com.klp.hub.inventory.domain.event.CouponCancelledEvent;
import com.klp.hub.inventory.domain.event.CouponUsedEvent;
import com.klp.hub.inventory.domain.repository.dto.InventoryDeduct;
import com.klp.hub.inventory.exception.InventoryErrorCode;
import com.klp.hub.inventory.infrastructure.lock.DistributedLockManager;
import com.klp.hub.inventory.presentation.dto.response.InventoryDeductResponseForEvent;
import com.klp.hub.inventory.presentation.dto.response.InventoryReplenishResponse;
import com.klp.hub.inventory.presentation.dto.response.InventoryReservationResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class InventoryFacade {

    private final InventoryService inventoryService;
    private final InventoryReservationService inventoryReservationService;
    private final InventoryCacheService cacheService;
    private final DistributedLockManager lockManager;

    /**
     * 재고 선점
     */
    public InventoryReservationResponse reserve(InventoryReservationCommand command) {

        if (inventoryReservationService.existsByIdempotencyKey(command.idempotencyKey())) {
            return InventoryReservationResponse.already();
        }

        Map<Boolean, List<ReservationItem>> partitioned = command.items().stream()
            .collect(Collectors.partitioningBy(
                item -> cacheService.existsCache(item.productId(), item.hubId())
            ));

        List<ReservationItem> hotItems = partitioned.get(true);
        List<ReservationItem> normalItems = partitioned.get(false);
        List<ReservationItem> reservedItems = new ArrayList<>();
        List<ReservationItem> fallbackItems = new ArrayList<>();

        try {
            // Hot Product 처리 (락 없이)
            // Redis에서 처리한 아이템과 처리하지 못한 아이템을 구분해서 반환
            if (!hotItems.isEmpty()) {
                Map<Boolean, List<ReservationItem>> cacheResult = reserveFromCache(hotItems);
                reservedItems = cacheResult.get(true);
                fallbackItems = cacheResult.get(false);
            }

            // 일반 상품 + 폴백 아이템 처리 (락 걸고)
            List<ReservationItem> dbItems = new ArrayList<>(normalItems);
            dbItems.addAll(fallbackItems);

            if (!dbItems.isEmpty()) {
                reserveFromDatabase(command.orderId(), command.idempotencyKey(), dbItems);
            }

            return InventoryReservationResponse.success(command.orderId());
        } catch (Exception e) {
            rollbackCacheReservations(reservedItems);
            throw e;
        }
    }

    private Map<Boolean, List<ReservationItem>> reserveFromCache(List<ReservationItem> items) {
        List<ReservationItem> reserved = new ArrayList<>();
        List<ReservationItem> fallback = new ArrayList<>();

        for (ReservationItem item : items) {
            long result = cacheService.reserveInventory(item.productId(), item.hubId(), item.quantity());

            if (result == NEED_DB_FALLBACK) {
                fallback.add(item);
            } else if (result == RESULT_INSUFFICIENT_STOCK) {
                rollbackCacheReservations(reserved);
                throw new BusinessException(InventoryErrorCode.INSUFFICIENT_STOCK);
            } else {
                reserved.add(item);
                // 선점 레코드 저장 + DB 동기화 이벤트 발행?
            }
        }

        return Map.of(true, reserved, false, fallback);
    }

    private void reserveFromDatabase(UUID orderId, String idempotencyKey, List<ReservationItem> items) {
        String lockKey = "inventory:reserve:" + orderId.toString();

        lock(lockKey);
        try {
            inventoryReservationService.reserve(orderId, idempotencyKey, items);
        } finally {
            unLock(lockKey);
        }
    }

    private void rollbackCacheReservations(List<ReservationItem> items) {
        for (ReservationItem item : items) {
            cacheService.restoreInventory(item.productId(), item.hubId(), item.quantity());
        }
    }

    public InventoryDeductResponseForEvent deduct(CouponUsedEvent event) {
        String idempotencyKey = event.inventoryIdempotencyKey();

        lock(idempotencyKey);
        try {
            List<InventoryDeduct> plans =
                InventoryUpdatePlanner.planDeductFromCouponUsed(event.products());
            return inventoryService.deductWithEventPublishing(event, plans);
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
