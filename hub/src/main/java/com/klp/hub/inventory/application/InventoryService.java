package com.klp.hub.inventory.application;

import com.klp.common.exception.BusinessException;
import com.klp.hub.inventory.application.dto.InventoryDeductCommand;
import com.klp.hub.inventory.application.dto.InventoryReplenishCommand;
import com.klp.hub.inventory.domain.Inventory;
import com.klp.hub.inventory.domain.InventoryIdempotencyStatus;
import com.klp.hub.inventory.domain.repository.InventoryRepository;
import com.klp.hub.inventory.domain.repository.dto.InventoryDeduct;
import com.klp.hub.inventory.domain.repository.dto.InventoryReplenish;
import com.klp.hub.inventory.domain.repository.exception.UniqueConstraintException;
import com.klp.hub.inventory.exception.InventoryErrorCode;
import com.klp.hub.inventory.infrastructure.lock.DistributedLockManager;
import com.klp.hub.inventory.presentation.dto.InventoryDeductResponse;
import com.klp.hub.inventory.presentation.dto.InventoryReplenishResponse;
import com.klp.hub.inventory.presentation.dto.InventoryResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    private final DistributedLockManager lockManager;

    @Transactional(readOnly = true)
    public InventoryResponse getByProductId(UUID productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId).orElseThrow(() -> {
            log.error("해당 상품의 재고를 찾을 수 없습니다. productId={}", productId);
            return new BusinessException(InventoryErrorCode.NOT_FOUND_INVENTORY);
        });

        return new InventoryResponse(
            productId,
            inventory.getId(),
            inventory.getHubId(),
            inventory.getQuantity()
        );
    }

    @Transactional
    public UUID create(UUID productId, UUID hubId, Integer quantity) {
        try {
            Inventory savedInventory = inventoryRepository.save(
                new Inventory(productId, hubId, quantity)
            );
            return savedInventory.getId();
        } catch (UniqueConstraintException exception) {
            log.error("이미 해당 재고가 존재합니다.");
            throw new BusinessException(InventoryErrorCode.INVENTORY_ALREADY_EXISTS);
        }
    }

    /**
     * 상품의 재고를 일괄 차감시킨다
     */
    @Transactional
    public InventoryDeductResponse deduct(InventoryDeductCommand command) {
        String idempotencyKey = command.idempotencyKey();
        boolean locked = lockManager.tryLock(idempotencyKey);
        if (!locked) {
            log.warn("이미 해당 멱등키로 재고 차감 진행 중 [Redis 락 획득 실패] idempotencyKey = {}", idempotencyKey);
            throw new BusinessException(InventoryErrorCode.IDEMPOTENCY_ALREADY_PROCESSING);
        }

        InventoryIdempotencyStatus status = inventoryRepository.acquireIdempotencyKey(
            idempotencyKey
        );

        if (status.isUsed()) {
            log.info("이미 성공 처리된 멱등키 입니다. idempotencyKey = {}", idempotencyKey);
            return InventoryDeductResponse.already();
        }

        List<InventoryDeduct> plans = InventoryUpdatePlanner.planDeduct(
            command.products()
        );
        int updated = inventoryRepository.deductAll(plans);
        if (updated != command.size()) {
            log.error("재고가 부족합니다.");
            throw new BusinessException(InventoryErrorCode.INSUFFICIENT_STOCK);
        }
        inventoryRepository.idempotencySuccess(idempotencyKey);

        return InventoryDeductResponse.success();
    }

    /**
     * 상품의 재고를 일괄 증가시킨다
     */
    @Transactional
    public InventoryReplenishResponse replenish(InventoryReplenishCommand command) {
        String idempotencyKey = command.idempotencyKey();
        boolean locked = lockManager.tryLock(idempotencyKey);
        if (!locked) {
            log.warn("이미 해당 멱등키로 재고 증가 진행 중 [Redis 락 획득 실패] idempotencyKey = {}", idempotencyKey);
            throw new BusinessException(InventoryErrorCode.IDEMPOTENCY_ALREADY_PROCESSING);
        }

        InventoryIdempotencyStatus status = inventoryRepository.acquireIdempotencyKey(
            command.idempotencyKey()
        );

        if (status.isUsed()) {
            log.info("이미 성공 처리된 멱등키 입니다. idempotencyKey = {}", idempotencyKey);
            return InventoryReplenishResponse.already();
        }

        List<InventoryReplenish> plans = InventoryUpdatePlanner.planReplenish(
            command.products()
        );
        int updated = inventoryRepository.replenishAll(plans);
        if (updated != command.size()) {
            log.error("증가하려는 일부 재고를 찾을 수 없습니다.");
            throw new BusinessException(InventoryErrorCode.PARTIAL_INVENTORY_NOT_FOUND);
        }
        inventoryRepository.idempotencySuccess(idempotencyKey);

        return InventoryReplenishResponse.success();
    }

    @Transactional
    public UUID delete(UUID inventoryId) {
        Inventory inventory = getById(inventoryId);

        inventory.delete(1L);

        return inventory.getId();
    }

    private Inventory getById(UUID inventoryId) {
        return inventoryRepository.findById(inventoryId).orElseThrow(() -> {
            log.error("재고가 존재하지 않습니다. inventoryId = {}", inventoryId);
            return new BusinessException(InventoryErrorCode.NOT_FOUND_INVENTORY);
        });
    }
}
