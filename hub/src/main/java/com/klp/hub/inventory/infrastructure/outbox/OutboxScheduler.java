package com.klp.hub.inventory.infrastructure.outbox;

import com.klp.hub.inventory.domain.outbox.InventoryOutbox;
import com.klp.hub.inventory.domain.outbox.InventoryOutboxRepository;
import com.klp.hub.inventory.infrastructure.kafka.config.KafkaTopicConfig;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxScheduler {

    private final InventoryOutboxRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final int BATCH_SIZE = 100;
    private static final int MAX_RETRY = 3;

    @Scheduled(fixedDelay = 1000)
    @SchedulerLock(name = "outbox_scheduler", lockAtMostFor = "PT30S", lockAtLeastFor = "PT5S")
    @Transactional
    public void publishPendingEvents() {
        List<InventoryOutbox> pendingEvents = outboxRepository.findPendingEvents(BATCH_SIZE);

        for (InventoryOutbox outbox : pendingEvents) {
            try {
                kafkaTemplate.send(
                    KafkaTopicConfig.INVENTORY_EVENTS,
                    outbox.getOrderId().toString(),
                    outbox.getPayload()
                ).get();

                outboxRepository.markAsPublished(outbox.getId());
                log.info("Outbox 이벤트 발행 성공: outboxId={}, eventType={}",
                    outbox.getId(), outbox.getEventType());

            } catch (Exception e) {
                log.error("Outbox 이벤트 발행 실패: outboxId={}, error={}",
                    outbox.getId(), e.getMessage());

                if (!outbox.isRetryable(MAX_RETRY)) {
                    outboxRepository.markAsFailed(outbox.getId());
                    log.warn("Outbox 이벤트 최대 재시도 초과: outboxId={}", outbox.getId());
                }
            }
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    @SchedulerLock(name = "outbox_cleanup", lockAtMostFor = "PT10M", lockAtLeastFor = "PT1M")
    @Transactional
    public void cleanupPublishedEvents() {
        outboxRepository.deletePublishedEvents();
        log.info("발행 완료된 Outbox 이벤트 정리 완료");
    }
}
