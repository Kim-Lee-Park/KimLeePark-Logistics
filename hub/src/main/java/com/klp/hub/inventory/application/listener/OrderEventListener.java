package com.klp.hub.inventory.application.listener;

import com.klp.hub.inventory.application.InventoryFacade;
import com.klp.hub.inventory.domain.event.OrderCreatedEvent;
import com.klp.hub.inventory.infrastructure.kafka.config.KafkaTopicConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final InventoryFacade inventoryFacade;

    @KafkaListener(
        topics = KafkaTopicConfig.ORDER_CREATED_EVENTS,
        groupId = "inventory-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderEvent(
        @Payload OrderCreatedEvent orderEvent
    ) {
        log.info("Order 이벤트 수신: orderId={}", orderEvent.orderId());
        try {
            inventoryFacade.deduct(orderEvent);
        } catch (Exception e) {
            log.error("Order 이벤트 처리 실패: orderId={}, error={}",
                orderEvent.orderId(),
                e.getMessage(),
                e
            );
        }
    }
}
