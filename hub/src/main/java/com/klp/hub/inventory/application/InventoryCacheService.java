package com.klp.hub.inventory.application;

import java.util.Optional;
import java.util.UUID;

public interface InventoryCacheService {

    /**
     * 캐시 키 존재 여부 확인
     */
    boolean existsCache(UUID productId, UUID hubId);

    /**
     * Lua Script로 재고 선점 (원자적 차감)
     *
     * @return 성공 시 남은 재고, 재고부족 시 -1, DB 폴백 필요 시 -2
     */
    long reserveInventory(UUID productId, UUID hubId, int quantity);

    /**
     * Lua Script로 재고 복구 (원자적 증가)
     *
     * @return 복구 후 재고, DB 폴백 필요 시 -2
     */
    long restoreInventory(UUID productId, UUID hubId, int quantity);

    /**
     * 현재 캐시 재고 조회
     */
    Optional<Integer> getInventory(UUID productId, UUID hubId);

    /**
     * 캐시 재고 설정
     */
    void setInventory(UUID productId, UUID hubId, int quantity, long ttlSeconds);

    /**
     * 캐시 삭제
     */
    void deleteCache(UUID productId, UUID hubId);

    /**
     * 캐시 TTL 획득
     */
    Long getTtl(UUID productId, UUID hubId);

}
