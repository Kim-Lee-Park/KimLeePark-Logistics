package com.klp.hub.inventory.application.listener;

import com.klp.hub.inventory.application.InventoryFacade;
import com.klp.hub.inventory.domain.event.OrderCreatedEvent;
import com.klp.hub.inventory.infrastructure.kafka.config.KafkaTopicConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@KafkaListener(
    topics = KafkaTopicConfig.ORDER_CREATED_EVENTS,
    groupId = "inventory-service-group",
    containerFactory = "kafkaListenerContainerFactory"
)
public class OrderCreatedEventListener {

    private final InventoryFacade inventoryFacade;

    @KafkaHandler
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Order 이벤트 수신: orderId={}", event.orderId());
        inventoryFacade.deduct(event);
    }

    @KafkaHandler(isDefault = true)
    public void handleUnknown(Object event) {
        log.warn("알 수 없는 이벤트 타입 수신: {}", event.getClass().getSimpleName());
    }
}
