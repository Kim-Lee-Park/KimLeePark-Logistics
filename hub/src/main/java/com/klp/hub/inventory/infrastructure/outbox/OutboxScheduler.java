package com.klp.hub.inventory.infrastructure.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.hub.inventory.domain.event.InventoryDbSyncEvent;
import com.klp.hub.inventory.domain.event.InventoryDeductedEvent;
import com.klp.hub.inventory.domain.event.InventoryDeductedFailedEvent;
import com.klp.hub.inventory.domain.event.InventoryReplenishedEvent;
import com.klp.hub.inventory.domain.outbox.InventoryOutbox;
import com.klp.hub.inventory.domain.outbox.InventoryOutboxRepository;
import com.klp.hub.inventory.infrastructure.kafka.config.KafkaTopicConfig;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class OutboxScheduler {

    private final InventoryOutboxRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final int BATCH_SIZE = 100;
    private static final int MAX_RETRY = 3;

    private static final String DEDUCT_EVENT_TYPE = "InventoryDeductedEvent";
    private static final String DEDUCT_FAILED_EVENT_TYPE = "InventoryDeductedFailedEvent";
    private static final String REPLENISH_EVENT_TYPE = "InventoryReplenishedEvent";
    private static final String DB_SYNC_EVENT_TYPE = "InventoryDbSyncEvent";

    public OutboxScheduler(
        InventoryOutboxRepository outboxRepository,
        @Qualifier("inventoryKafkaTemplate") KafkaTemplate<String, Object> kafkaTemplate,
        ObjectMapper objectMapper
    ) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 1000)
    @SchedulerLock(name = "outbox_scheduler", lockAtMostFor = "PT30S", lockAtLeastFor = "PT5S")
    @Transactional
    public void publishPendingEvents() {
        List<InventoryOutbox> pendingEvents = outboxRepository.findPendingEvents(BATCH_SIZE);

        for (InventoryOutbox outbox : pendingEvents) {
            try {
                Object event = deserializeEvent(outbox.getEventType(), outbox.getPayload());

                String topic = getTopicByEventType(outbox.getEventType());

                kafkaTemplate.send(
                    topic,
                    outbox.getOrderId().toString(),
                    event
                ).get();

                outboxRepository.markAsPublished(outbox.getId());
                log.info("Outbox 이벤트 발행 성공: outboxId={}, eventType={}, topic={}",
                    outbox.getId(), outbox.getEventType(), topic);

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

    private String getTopicByEventType(String eventType) {
        return switch (eventType) {
            case DEDUCT_EVENT_TYPE -> KafkaTopicConfig.INVENTORY_DEDUCTED_TOPIC;
            case DEDUCT_FAILED_EVENT_TYPE -> KafkaTopicConfig.INVENTORY_DEDUCTED_FAILED_TOPIC;
            case REPLENISH_EVENT_TYPE -> KafkaTopicConfig.INVENTORY_REPLENISHED_TOPIC;
            case DB_SYNC_EVENT_TYPE -> KafkaTopicConfig.INVENTORY_DB_SYNC_TOPIC;
            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        };
    }


    private Object deserializeEvent(String eventType, String payload) throws Exception {
        return switch (eventType) {
            case DEDUCT_EVENT_TYPE -> objectMapper.readValue(payload, InventoryDeductedEvent.class);
            case DEDUCT_FAILED_EVENT_TYPE -> objectMapper.readValue(payload, InventoryDeductedFailedEvent.class);
            case REPLENISH_EVENT_TYPE -> objectMapper.readValue(payload, InventoryReplenishedEvent.class);
            case DB_SYNC_EVENT_TYPE -> objectMapper.readValue(payload, InventoryDbSyncEvent.class);
            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        };
    }
}