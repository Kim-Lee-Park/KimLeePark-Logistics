package com.klp.hub.inventory.application;

import com.klp.hub.inventory.application.dto.InventoryDeductCommand;
import com.klp.hub.inventory.domain.Inventory;
import com.klp.hub.inventory.domain.repository.InventoryRepository;
import com.klp.hub.inventory.presentation.dto.InventoryDeductResponse;
import com.klp.hub.inventory.presentation.dto.InventoryResponse;
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

    @Transactional(readOnly = true)
    public InventoryResponse getByProductId(UUID productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId).orElseThrow(() -> {
            log.error("해당 상품의 재고를 찾을 수 없습니다. productId={}", productId);
            // FIXME: 도메인 예외가 추가되면 수정 필요
            return new RuntimeException();
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
        Inventory savedInventory = inventoryRepository.save(
            new Inventory(productId, hubId, quantity)
        );
        return savedInventory.getId();
    }

    /**
     * 상품의 재고를 일괄 차감시킨다
     */
    @Transactional
    public InventoryDeductResponse deduct(InventoryDeductCommand command) {
        boolean acquired = inventoryRepository.tryAcquireIdempotencyKey(command.idempotencyKey());
        if (!acquired) {
            log.warn("이미 처리된 요청입니다.");
            return InventoryDeductResponse.already();
        }

        int updated = inventoryRepository.deductAll(command.toInventoryDeductList());
        if (updated != command.size()) {
            log.error("재고가 부족합니다.");
            throw new RuntimeException();
        }
        return InventoryDeductResponse.success();
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
            // FIXME: 도메인 예외 전까지 임시 예외처리
            throw new RuntimeException();
        });
    }
}
