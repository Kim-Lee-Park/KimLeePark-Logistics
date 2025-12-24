package com.klp.hub.inventory.application;

import com.klp.hub.inventory.domain.event.InventoryDbSyncEvent;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventorySyncBuffer {

    private final OutboxService outboxService;

    private final ConcurrentLinkedQueue<InventoryDbSyncEvent> buffer = new ConcurrentLinkedQueue<>();

    private final AtomicLong totalEnqueued = new AtomicLong(0);
    private final AtomicLong totalProcessed = new AtomicLong(0);

    public void enqueue(InventoryDbSyncEvent event) {
        buffer.offer(event);
        totalEnqueued.incrementAndGet();
        log.info("Buffer 추가: orderId={}, size={}", event.orderId(), buffer.size());
    }

    public List<InventoryDbSyncEvent> drainBatch(int maxSize) {
        List<InventoryDbSyncEvent> batch = new ArrayList<>(maxSize);

        for (int i = 0; i < maxSize; i++) {
            InventoryDbSyncEvent event = buffer.poll();
            if (event == null) {
                break;
            }
            batch.add(event);
        }

        totalProcessed.addAndGet(batch.size());
        return batch;
    }

    @PreDestroy
    public void onShutdown() {
        List<InventoryDbSyncEvent> remaining = drainBatch(Integer.MAX_VALUE);
        if (!remaining.isEmpty()) {
            log.warn("Shutdown으로 인한 이벤트 저장: count={}", remaining.size());
            List<InventoryDbSyncEvent> failed = outboxService.saveInventoryDbSyncEventBatch(remaining);
            if (!failed.isEmpty()) {
                log.error("Shutdown 중 저장 실패: count={}", failed.size());
            }
        }
    }


    public int size() {
        return buffer.size();
    }

    public long getTotalEnqueued() {
        return totalEnqueued.get();
    }

    public long getTotalProcessed() {
        return totalProcessed.get();
    }
}
