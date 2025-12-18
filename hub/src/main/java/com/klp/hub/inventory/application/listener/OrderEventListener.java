package com.klp.hub.inventory.application.listener;

import com.klp.hub.inventory.application.InventoryFacade;
import com.klp.hub.inventory.domain.event.OrderCancelledEvent;
import com.klp.hub.inventory.domain.event.OrderCreatedEvent;
import com.klp.hub.inventory.domain.event.OrderFailedEvent;
import com.klp.hub.inventory.domain.event.WhichRollback;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Component
@RequiredArgsConstructor
@KafkaListener(
    topics = "order.topic",
    groupId = "inventory-service-group",
    containerFactory = "kafkaListenerContainerFactory"
)
public class OrderEventListener {

    private final InventoryFacade inventoryFacade;

    @KafkaHandler
    @Transactional
    public void handleOrderFailed(
        @Payload OrderFailedEvent event,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment
    ) {
        log.info("=== 주문 실패 이벤트 수신: orderId={}, partition={}, offset={} ===",
            event.orderId(), partition, offset);
        try {
            UUID orderId = event.orderId();
            WhichRollback rollbackType = event.type();

            if (rollbackType == WhichRollback.INVENTORY || rollbackType == WhichRollback.ALL) {
                log.info("재고 선점 해제 시작 - orderId: {}", orderId);
                inventoryFacade.release(orderId);
                log.info("재고 선점 해제 완료 - orderId: {}", orderId);
            } else {
                log.info("재고 선점 해제 불필요 - orderId: {}, rollbackType: {}",
                    orderId, rollbackType);
            }

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.info("오프셋 커밋 완료: orderId={}, offset={}", orderId, offset);
            }

        } catch (Exception e) {
            log.error("주문 실패 이벤트 처리 실패: orderId={}, partition={}, offset={}, error={}",
                event.orderId(), partition, offset, e.getMessage(), e);
            throw e;
        }
    }

    @KafkaHandler
    @Transactional
    public void handleOrderCreated(
        @Payload OrderCreatedEvent event
    ) {
        log.info("주문이 생성되었으니 재고 차감 준비");
    }

    @KafkaHandler
    @Transactional
    public void handleOrderCancelled(
        @Payload OrderCancelledEvent event
    ) {
        log.info("주문이 취소되었으니 재고 복구 준비");
    }

    @KafkaHandler(isDefault = true)
    public void handleUnknown(Object event) {
        log.warn("알 수 없는 Order 이벤트 타입 수신: {}", event.getClass().getSimpleName());
    }

    @DltHandler
    public void handleOrderEventDlt(
        @Payload Object event,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        @Header(KafkaHeaders.EXCEPTION_MESSAGE) String exceptionMessage
    ) {
        log.error("========================================");
        log.error("⚠️ DLT 도착: Order Event");
        log.error("⚠️ 수동 처리가 필요합니다!");
        log.error("========================================");
        log.error("Topic: {}", topic);
        log.error("EventType: {}", event.getClass().getSimpleName());
        log.error("Error: {}", exceptionMessage);
        log.error("Event Details: {}", event);

    }
}
