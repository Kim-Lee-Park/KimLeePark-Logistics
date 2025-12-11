package com.klp.delivery.delivery.infrastructure.listener;

import com.klp.delivery.delivery.application.facade.DeliveryFacade;
import com.klp.delivery.delivery.domain.event.InventoryReplenishedEvent;
import com.klp.delivery.delivery.domain.repository.DeliveryOutboxEventRepository;
import com.klp.delivery.delivery.infrastructure.outbox.OutboxScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryReplenishedEventListener {

    private final DeliveryFacade deliveryFacade;
    private final DeliveryOutboxEventRepository deliveryOutboxEventRepository;

    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000L, multiplier = 2.0, maxDelay = 4000L),
        autoCreateTopics = "true",
        include = Exception.class,
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(
        topics = "inventory.topic",
        groupId = "delivery-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleInventoryReplenished(
        @Payload InventoryReplenishedEvent event,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment) {

        log.info("=== 재고 복구 이벤트 수신: orderId={}, partition={}, offset={}, cancelReason={} ===",
            event.orderId(), partition, offset, event.cancelReason());

        try {
            Long deletedBy = event.userId() != null ? event.userId() : 0L;
            deliveryFacade.cancelDeliveriesByOrderId(event.orderId(), deletedBy);
            log.info("=== 배송 취소 완료: orderId={} ===", event.orderId());

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.info("오프셋 커밋 완료: orderId={}, offset={}", event.orderId(), offset);
            }

        } catch (Exception e) {
            log.error("재고 복구 이벤트 처리 실패: orderId={}, partition={}, offset={}",
                event.orderId(), partition, offset, e);
            throw e;
        }
    }

    @DltHandler
    public void handleInventoryReplenishedDlt(
        @Payload InventoryReplenishedEvent event,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        @Header(KafkaHeaders.EXCEPTION_MESSAGE) String exceptionMessage) {

        log.error("========================================");
        log.error("⚠️ DLT 도착: Inventory Replenished Event");
        log.error("⚠️ 수동 처리가 필요합니다!");
        log.error("========================================");
        log.error("orderId={}, error={}", event.orderId(), exceptionMessage);
    }
}

