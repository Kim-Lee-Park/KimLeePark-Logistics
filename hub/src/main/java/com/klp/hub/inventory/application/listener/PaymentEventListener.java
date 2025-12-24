package com.klp.hub.inventory.application.listener;

import com.klp.hub.inventory.application.InventoryFacade;
import com.klp.hub.inventory.domain.event.PaymentFailedEvent;
import com.klp.hub.inventory.infrastructure.kafka.config.KafkaTopicConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final InventoryFacade inventoryFacade;

    @KafkaListener(
        topics = KafkaTopicConfig.PAYMENT_FAILED_TOPIC,
        groupId = "inventory-service-group",
        containerFactory = "inventoryKafkaListenerContainerFactory"
    )
    public void handlePaymentFailed(@Payload PaymentFailedEvent event) {
        log.info("결제 실패 이벤트 수신: orderId={}, reason={}", event.orderId(), event.reason());
        inventoryFacade.release(event.orderId());
    }

    @KafkaListener(
        topics = KafkaTopicConfig.PAYMENT_FAILED_DLT,
        groupId = "inventory-service-group-dlt",
        containerFactory = "inventoryKafkaListenerContainerFactory"
    )
    public void handlePaymentFailedDlt(@Payload PaymentFailedEvent event) {
        log.error("========================================");
        log.error("⚠️ DLT 도착: PaymentFailed");
        log.error("⚠️ 결제 실패 처리 실패 - 수동 처리 필요!");
        log.error("========================================");
        log.error("orderId={}", event.orderId());
    }
}
