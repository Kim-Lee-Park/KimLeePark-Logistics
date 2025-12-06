package com.klp.hub.inventory.application.listener;

import com.klp.hub.inventory.application.InventoryFacade;
import com.klp.hub.inventory.domain.event.PaymentCancelledEvent;
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
        topics = KafkaTopicConfig.PAYMENT_EVENTS,
        groupId = "inventory-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentFailedEvent(@Payload PaymentFailedEvent event) {
        log.info("결제 실패 이벤트 수신: orderId={}, reason={}", event.orderId(), event.reason());
        try {
            inventoryFacade.release(event.orderId());
        } catch (Exception e) {
            log.error("결제 실패 이벤트 처리 실패: orderId={}, error={}",
                event.orderId(),
                e.getMessage(),
                e
            );
            throw e;
        }
    }

    @KafkaListener(
        topics = KafkaTopicConfig.PAYMENT_EVENTS,
        groupId = "inventory-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentCancelledEvent(@Payload PaymentCancelledEvent event) {
        log.info("결제 취소 이벤트 수신: orderId={}", event.orderId());
        try {
            inventoryFacade.replenishFromCancellation(event);
        } catch (Exception e) {
            log.error("결제 취소 이벤트 처리 실패: orderId={}, error={}",
                event.orderId(),
                e.getMessage(),
                e
            );
            throw e;
        }
    }
}
