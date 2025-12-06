package com.klp.hub.inventory.application.listener;

import com.klp.hub.inventory.application.InventoryFacade;
import com.klp.hub.inventory.domain.event.CouponUsedEvent;
import com.klp.hub.inventory.infrastructure.kafka.config.KafkaTopicConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponUsedEventListener {

    private final InventoryFacade inventoryFacade;

    @KafkaListener(
        topics = KafkaTopicConfig.COUPON_EVENTS,
        groupId = "inventory-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleCouponUsedEvent(@Payload CouponUsedEvent event) {
        log.info("쿠폰 사용 이벤트 수신: orderId={}", event.orderId());
        try {
            inventoryFacade.confirm(event.orderId());
        } catch (Exception e) {
            log.error("쿠폰 사용 이벤트 처리 실패: orderId={}, error={}",
                event.orderId(),
                e.getMessage(),
                e
            );
            throw e;
        }
    }
}
