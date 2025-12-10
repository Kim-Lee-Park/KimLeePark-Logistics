package com.klp.hub.inventory.application.listener;

import com.klp.hub.inventory.application.InventoryFacade;
import com.klp.hub.inventory.domain.event.CouponUsedEvent;
import com.klp.hub.inventory.domain.event.CouponUsedFailedEvent;
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
    topics = KafkaTopicConfig.COUPON_EVENTS,
    groupId = "inventory-service-group",
    containerFactory = "inventoryKafkaListenerContainerFactory"
)
public class CouponEventListener {

    private final InventoryFacade inventoryFacade;

    @KafkaHandler
    public void handleCouponUsed(CouponUsedEvent event) {
        log.info("쿠폰 사용 이벤트 수신: orderId={}", event.orderId());
        inventoryFacade.confirm(event.orderId());
    }

    @KafkaHandler
    public void handleCouponUsedFailed(CouponUsedFailedEvent event) {
        log.info("쿠폰 사용실패 이벤트 수신: orderId={}", event.orderId());
        inventoryFacade.release(event.orderId());
    }

    @KafkaHandler(isDefault = true)
    public void handleUnknown(Object event) {
        log.warn("알 수 없는 이벤트 타입 수신: {}", event.getClass().getSimpleName());
    }
}
