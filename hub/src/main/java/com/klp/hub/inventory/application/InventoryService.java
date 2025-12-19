package com.klp.hub.inventory.application;

import com.klp.hub.global.exception.BusinessException;
import com.klp.hub.inventory.application.dto.InventoryReplenishCommand;
import com.klp.hub.inventory.domain.Inventory;
import com.klp.hub.inventory.domain.InventoryIdempotencyStatus;
import com.klp.hub.inventory.domain.event.CouponCancelledEvent;
import com.klp.hub.inventory.domain.event.CouponUsedEvent;
import com.klp.hub.inventory.domain.event.InventoryDeductedEvent;
import com.klp.hub.inventory.domain.event.InventoryDeductedFailedEvent;
import com.klp.hub.inventory.domain.event.InventoryReplenishedEvent;
import com.klp.hub.inventory.domain.repository.InventoryRepository;
import com.klp.hub.inventory.domain.repository.dto.InventoryDeduct;
import com.klp.hub.inventory.domain.repository.dto.InventoryReplenish;
import com.klp.hub.inventory.domain.repository.exception.UniqueConstraintException;
import com.klp.hub.inventory.exception.InventoryErrorCode;
import com.klp.hub.inventory.presentation.dto.response.InventoryDeductResponse;
import com.klp.hub.inventory.presentation.dto.response.InventoryDeductResponseForEvent;
import com.klp.hub.inventory.presentation.dto.response.InventoryReplenishResponse;
import com.klp.hub.inventory.presentation.dto.response.InventoryResponse;
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
    private final OutboxService outboxService;

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
    public InventoryDeductResponse deduct(CouponUsedEvent event) {
        String idempotencyKey = event.inventoryIdempotencyKey();
        InventoryIdempotencyStatus status = inventoryRepository.acquireIdempotencyKey(
            idempotencyKey
        );

        if (status.isUsed()) {
            log.info("이미 성공 처리된 멱등키 입니다. idempotencyKey = {}", idempotencyKey);
            return InventoryDeductResponse.already();
        }

        List<InventoryDeduct> plans = InventoryUpdatePlanner.planDeductFromCouponUsed(
            event.products());

        int updated = inventoryRepository.deductAll(plans);
        if (updated != plans.size()) {
            log.error("재고가 부족합니다.");
            throw new BusinessException(InventoryErrorCode.INSUFFICIENT_STOCK);
        }
        inventoryRepository.idempotencySuccess(idempotencyKey);

        InventoryDeductedEvent deductedEvent = InventoryDeductedEvent.of(event);
        outboxService.saveInventoryDeductedEvent(deductedEvent);

        return InventoryDeductResponse.success();
    }

    /**
     * 상품의 재고를 일괄 증가시킨다
     */
    @Transactional
    public InventoryReplenishResponse replenish(InventoryReplenishCommand command) {
        String idempotencyKey = command.idempotencyKey();
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

    /**
     * 결제 취소 시 재고 복원
     */
    @Transactional
    public void replenishFromCancellation(CouponCancelledEvent event) {
        String idempotencyKey = event.inventoryIdempotencyKey();
        InventoryIdempotencyStatus status = inventoryRepository.acquireIdempotencyKey(
            idempotencyKey);

        if (status.isUsed()) {
            log.info("이미 처리된 결제 취소 복원 요청입니다. idempotencyKey = {}", idempotencyKey);
            return;
        }

        List<InventoryReplenish> plans = event.products().stream()
            .map(item -> new InventoryReplenish(item.productId(), item.hubId(), item.quantity()))
            .toList();

        int updated = inventoryRepository.replenishAll(plans);
        if (updated != plans.size()) {
            log.error("복원하려는 일부 재고를 찾을 수 없습니다. orderId={}", event.orderId());
            throw new BusinessException(InventoryErrorCode.PARTIAL_INVENTORY_NOT_FOUND);
        }
        inventoryRepository.idempotencySuccess(idempotencyKey);

        InventoryReplenishedEvent replenishedEvent = InventoryReplenishedEvent.of(event);
        outboxService.saveInventoryReplenishedEvent(replenishedEvent);

        log.info("결제 취소로 인한 재고 복원 완료. orderId={}", event.orderId());
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

    @Transactional
    public InventoryDeductResponseForEvent deductWithEventPublishing(
        CouponUsedEvent event,
        List<InventoryDeduct> plans
    ) {
        String idempotencyKey = event.inventoryIdempotencyKey();

        // 1. 멱등키 확인
        InventoryIdempotencyStatus status = inventoryRepository.acquireIdempotencyKey(
            idempotencyKey
        );

        if (status.isUsed()) {
            log.info("이미 성공 처리된 멱등키입니다. idempotencyKey = {}", idempotencyKey);
            return InventoryDeductResponseForEvent.already();
        }

        // 2. 재고 차감 시도
        int updated = inventoryRepository.deductAll(plans);

        // 3-A. 재고 부족 시 → Failed Event 발행 (예외 발생 안함!)
        if (updated != plans.size()) {
            log.warn("재고 부족으로 차감 실패. orderId={}, requested={}, updated={}",
                event.orderId(), plans.size(), updated);

            // Failed Event를 Outbox에 저장
            InventoryDeductedFailedEvent failedEvent = InventoryDeductedFailedEvent.of(event);
            outboxService.saveInventoryDeductedFailedEvent(failedEvent);

            // 실패도 처리 완료로 간주
            inventoryRepository.idempotencySuccess(idempotencyKey);

            log.info("재고 부족 Failed Event 발행 완료: orderId={}", event.orderId());
            return InventoryDeductResponseForEvent.failed("재고 부족");
        }

        // 3-B. 성공 시 → Success Event 발행
        inventoryRepository.idempotencySuccess(idempotencyKey);

        InventoryDeductedEvent deductedEvent = InventoryDeductedEvent.of(event);
        outboxService.saveInventoryDeductedEvent(deductedEvent);

        log.info("재고 차감 Success Event 발행 완료: orderId={}", event.orderId());
        return InventoryDeductResponseForEvent.success();
    }

}
