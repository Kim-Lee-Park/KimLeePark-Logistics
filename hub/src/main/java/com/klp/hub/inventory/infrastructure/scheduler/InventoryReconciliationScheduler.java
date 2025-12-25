package com.klp.hub.inventory.infrastructure.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.hub.inventory.application.InventoryCacheService;
import com.klp.hub.inventory.domain.Inventory;
import com.klp.hub.inventory.domain.event.InventoryDbSyncEvent;
import com.klp.hub.inventory.domain.outbox.InventoryOutbox;
import com.klp.hub.inventory.domain.outbox.InventoryOutboxRepository;
import com.klp.hub.inventory.domain.repository.InventoryRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryReconciliationScheduler {

    private final StringRedisTemplate redisTemplate;
    private final InventoryCacheService cacheService;
    private final InventoryRepository inventoryRepository;
    private final InventoryOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    private static final String KEY_PREFIX = "inventory:";
    private static final int TOLERANCE_THRESHOLD = 10;

    @Scheduled(fixedDelay = 300000)
    public void reconcile() {
        log.info("Redis-DB Reconciliation 시작");

        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
        if (keys.isEmpty()) {
            log.info("캐시된 재고 없음, Reconciliation 스킵");
            return;
        }

        Map<String, Integer> pendingDeductions = calculatePendingDeductions();
        log.info("Pending Outbox 차감량 계산 완료: {} 건", pendingDeductions.size());

        int checked = 0;
        int mismatched = 0;
        int corrected = 0;

        for (String key : keys) {
            try {
                String[] parts = key.replace(KEY_PREFIX, "").split(":");
                if (parts.length != 2) {
                    continue;
                }

                UUID productId = UUID.fromString(parts[0]);
                UUID hubId = UUID.fromString(parts[1]);

                checked++;

                Optional<Integer> redisQty = cacheService.getInventory(productId, hubId);
                if (redisQty.isEmpty()) {
                    continue;
                }

                Optional<Inventory> dbInventory = inventoryRepository.findByProductIdAndHubId(productId, hubId);
                if (dbInventory.isEmpty()) {
                    log.warn("DB에 재고 없음, 캐시 삭제: productId={}, hubId={}", productId, hubId);
                    cacheService.deleteCache(productId, hubId);
                    corrected++;
                    continue;
                }

                int dbQty = dbInventory.get().getQuantity();

                String deductionKey = productId + ":" + hubId;
                int pendingDeduction = pendingDeductions.getOrDefault(deductionKey, 0);
                int expectedRedisQty = dbQty - pendingDeduction;

                int diff = Math.abs(redisQty.get() - expectedRedisQty);

                if (diff > TOLERANCE_THRESHOLD) {
                    mismatched++;
                    log.warn("재고 불일치 감지: productId={}, hubId={}, redis={}, expected={} (db={}, pending={}), diff={}",
                        productId, hubId, redisQty.get(), expectedRedisQty, dbQty, pendingDeduction, diff);

                    Long ttl = cacheService.getTtl(productId, hubId);
                    if (ttl != null && ttl > 0) {
                        cacheService.setInventory(productId, hubId, expectedRedisQty, ttl);
                        corrected++;
                        log.info("재고 보정 완료: productId={}, hubId={}, newQty={}", productId, hubId, expectedRedisQty);
                    }
                }

            } catch (Exception e) {
                log.error("Reconciliation 처리 중 오류: key={}, error={}", key, e.getMessage());
            }
        }

        log.info("Redis-DB Reconciliation 완료: checked={}, mismatched={}, corrected={}",
            checked, mismatched, corrected);
    }

    private Map<String, Integer> calculatePendingDeductions() {
        Map<String, Integer> deductions = new HashMap<>();

        List<InventoryOutbox> pendingEvents = outboxRepository.findPendingDbSyncEvents();

        for (InventoryOutbox outbox : pendingEvents) {
            try {
                InventoryDbSyncEvent event = objectMapper.readValue(
                    outbox.getPayload(), InventoryDbSyncEvent.class);

                for (InventoryDbSyncEvent.SyncItem item : event.items()) {
                    String key = item.productId() + ":" + item.hubId();
                    deductions.merge(key, item.quantity(), Integer::sum);
                }
            } catch (Exception e) {
                log.warn("Outbox payload 파싱 실패: outboxId={}, error={}",
                    outbox.getId(), e.getMessage());
            }
        }

        return deductions;
    }
}
