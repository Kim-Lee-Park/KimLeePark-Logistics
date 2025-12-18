package com.klp.hub.inventory.application.listener;

import com.klp.hub.inventory.application.InventoryFacade;
import com.klp.hub.inventory.domain.event.OrderFailedEvent;
import com.klp.hub.inventory.domain.event.WhichRollback;
import com.klp.hub.inventory.infrastructure.kafka.config.KafkaTopicConfig;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

public class OrderEventListener {

    private final InventoryFacade inventoryFacade;

    @KafkaListener(
        topics = KafkaTopicConfig.ORDER_FAILED_TOPIC,
        groupId = "inventory-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
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

    @KafkaListener(
        topics = KafkaTopicConfig.ORDER_FAILED_DLT,
        groupId = "inventory-service-group-dlt",
        containerFactory = "inventoryKafkaListenerContainerFactory"
    )
    public void handleOrderFailedDlt(@Payload OrderFailedEvent event) {
        log.error("========================================");
        log.error("⚠️ DLT 도착: OrderFailed");
        log.error("⚠️ 주문 실패 처리 실패 - 수동 처리 필요!");
        log.error("========================================");
        log.error("orderId={}, userCouponId={}", event.orderId(), event.userCouponId());
    }
}
