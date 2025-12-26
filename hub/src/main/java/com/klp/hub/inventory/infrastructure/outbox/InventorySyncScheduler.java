package com.klp.hub.inventory.infrastructure.outbox;

import com.klp.hub.inventory.application.InventorySyncBuffer;
import com.klp.hub.inventory.application.OutboxService;
import com.klp.hub.inventory.domain.event.InventoryDbSyncEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventorySyncScheduler {

    private final InventorySyncBuffer syncBuffer;
    private final OutboxService outboxService;

    private static final int BATCH_SIZE = 500;

    @Scheduled(fixedDelay = 100)
    public void processPendingSync() {
        List<InventoryDbSyncEvent> events = syncBuffer.drainBatch(BATCH_SIZE);

        if (events.isEmpty()) {
            return;
        }

        log.info("Outbox Worker 처리: count={}, remaining={}", events.size(), syncBuffer.size());

        List<InventoryDbSyncEvent> failed = outboxService.saveInventoryDbSyncEventBatch(events);

        if (!failed.isEmpty()) {
            log.warn("배치 저장 실패, 재시도 큐에 추가: count={}", failed.size());
            failed.forEach(syncBuffer::enqueue);
        }
    }
}
