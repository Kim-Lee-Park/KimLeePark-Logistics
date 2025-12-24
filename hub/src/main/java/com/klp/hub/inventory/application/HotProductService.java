package com.klp.hub.inventory.application;

import com.klp.hub.global.exception.BusinessException;
import com.klp.hub.inventory.domain.Inventory;
import com.klp.hub.inventory.domain.repository.InventoryRepository;
import com.klp.hub.inventory.exception.InventoryErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotProductService {

    private final InventoryCacheService cacheService;
    private final InventoryRepository inventoryRepository;

    /**
     * 인기 상품 등록
     */
    public void register(UUID productId, UUID hubId, Long ttlSeconds) {
        if (ttlSeconds == null || ttlSeconds <= 0) {
            throw new BusinessException(InventoryErrorCode.INVALID_REGISTER_TTL);
        }

        Inventory inventory = inventoryRepository.findByProductIdAndHubId(productId, hubId)
            .orElseThrow(() -> new BusinessException(InventoryErrorCode.NOT_FOUND_INVENTORY));

        cacheService.setInventory(productId, hubId, inventory.getQuantity(), ttlSeconds);

        log.info("인기 상품 등록 완료: productId={}, hubId={}, quantity={}, ttl={}sec",
            productId, hubId, inventory.getQuantity(), ttlSeconds);
    }

    /**
     * 인기 상품 해제
     */
    public void unregister(UUID productId, UUID hubId) {
        cacheService.deleteCache(productId, hubId);
        log.info("인기 상품 해제 완료: productId={}, hubId={}", productId, hubId);
    }

    /**
     * 현재 상품 재고 조회(Redis에서)
     */
    public int getQuantity(UUID productId, UUID hubId) {
        return cacheService.getInventory(productId, hubId).orElse(0);
    }

    /**
     * 캐시 재동기화 (DB → Redis) 기존 TTL 유지하면서 값만 업데이트
     */
    public void resync(UUID productId, UUID hubId) {
        if (!cacheService.existsCache(productId, hubId)) {
            throw new BusinessException(InventoryErrorCode.NOT_HOT_PRODUCT);
        }

        Inventory inventory = inventoryRepository.findByProductIdAndHubId(productId, hubId)
            .orElseThrow(() -> new BusinessException(InventoryErrorCode.NOT_FOUND_INVENTORY));

        Long ttl = cacheService.getTtl(productId, hubId);
        if (ttl == null || ttl <= 0) {
            throw new BusinessException(InventoryErrorCode.INVALID_REGISTER_TTL);
        }

        cacheService.setInventory(productId, hubId, inventory.getQuantity(), ttl);

        log.info("인기 상품 재동기화 완료: productId={}, hubId={}, quantity={}",
            productId, hubId, inventory.getQuantity());
    }

    /**
     * 캐시 재고 직접 설정
     */
    public void updateQuantity(UUID productId, UUID hubId, int quantity) {
        if (!cacheService.existsCache(productId, hubId)) {
            throw new BusinessException(InventoryErrorCode.NOT_HOT_PRODUCT);
        }

        Long ttl = cacheService.getTtl(productId, hubId);
        if (ttl == null || ttl <= 0) {
            throw new BusinessException(InventoryErrorCode.INVALID_REGISTER_TTL);
        }

        cacheService.setInventory(productId, hubId, quantity, ttl);

        log.info("인기 상품 재고 수정 완료: productId={}, hubId={}, quantity={}",
            productId, hubId, quantity);
    }
}
