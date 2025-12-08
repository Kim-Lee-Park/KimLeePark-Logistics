package com.klp.hub.inventory.application.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.hub.global.exception.BusinessException;
import com.klp.hub.inventory.application.InventoryFacade;
import com.klp.hub.inventory.domain.event.PaymentCancelledEvent;
import com.klp.hub.inventory.domain.event.PaymentFailedEvent;
import com.klp.hub.inventory.exception.InventoryErrorCode;
import com.klp.hub.inventory.infrastructure.kafka.config.KafkaTopicConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final InventoryFacade inventoryFacade;
    private final ObjectMapper objectMapper;

    private static final String EVENT_TYPE_HEADER = "eventType";
    private static final String PAYMENT_FAILED = "PaymentFailedEvent";
    private static final String PAYMENT_CANCELLED = "PaymentCancelledEvent";

    @KafkaListener(
        topics = KafkaTopicConfig.PAYMENT_EVENTS,
        groupId = "inventory-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentEvent(
        @Payload String payload,
        @Header(name = EVENT_TYPE_HEADER) String eventType
    ) {
        if (PAYMENT_FAILED.equals(eventType)) {
            PaymentFailedEvent event = deserialize(payload, PaymentFailedEvent.class);
            handlePaymentFailed(event);
        } else if (PAYMENT_CANCELLED.equals(eventType)) {
            PaymentCancelledEvent event = deserialize(payload, PaymentCancelledEvent.class);
            handlePaymentCancelled(event);
        } else {
            log.error("유효하지 않은 이벤트 타입: eventType={}", eventType);
            throw new BusinessException(InventoryErrorCode.INVALID_EVENT_TYPE);
        }
    }

    private <T> T deserialize(String payload, Class<T> clazz) {
        try {
            return objectMapper.readValue(payload, clazz);
        } catch (JsonProcessingException e) {
            log.error("이벤트 역직렬화 실패: payload={}, error={}", payload, e.getMessage());
            throw new BusinessException(InventoryErrorCode.EVENT_DESERIALIZATION_FAILED);
        }
    }

    private void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("결제 실패 이벤트 수신: orderId={}, reason={}", event.orderId(), event.reason());
        inventoryFacade.release(event.orderId());
    }

    private void handlePaymentCancelled(PaymentCancelledEvent event) {
        log.info("결제 취소 이벤트 수신: orderId={}", event.orderId());
        inventoryFacade.replenishFromCancellation(event);
    }
}
