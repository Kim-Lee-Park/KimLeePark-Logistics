package com.klp.promotion.coupon.infrastructure.kafka.listener;

import com.klp.promotion.coupon.application.service.UserCouponService;
import com.klp.promotion.coupon.domain.event.InventoryDeductedEvent;
import com.klp.promotion.coupon.domain.event.InventoryDeductedFailedEvent;
import com.klp.promotion.coupon.infrastructure.kafka.config.KafkaTopicConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Component
@RequiredArgsConstructor

public class InventoryEventListener {

    private final UserCouponService userCouponService;

    @KafkaListener(
        topics = KafkaTopicConfig.INVENTORY_DEDUCTED_FAILED_TOPIC,
        groupId = "coupon-service-group",
        containerFactory = "couponKafkaListenerContainerFactory"
    )
    @Transactional
    public void handleInventoryDeductedFailed(@Payload InventoryDeductedFailedEvent event) {
        log.info("재고 차감 실패 이벤트 수신: orderId={}", event.orderId());

        userCouponService.cancelReserve(event.orderId());
        // 선점 해제는 해서 여기다가 쿠폰 복구 기능 넣으면 될거 같습니다.
    }

    @KafkaListener(
        topics = KafkaTopicConfig.INVENTORY_DEDUCTED_FAILED_DLT,
        groupId = "coupon-service-group-dlt",
        containerFactory = "couponKafkaListenerContainerFactory"
    )
    public void handleInventoryDeductedFailedDlt(@Payload InventoryDeductedFailedEvent event) {
        log.error("========================================");
        log.error("⚠️ DLT 도착: InventoryDeductedFailedEvent");
        log.error("⚠️ 3회 재시도 후에도 실패했습니다!");
        log.error("⚠️ 수동 처리가 필요합니다!");
        log.error("========================================");
        log.error("orderId={}, userId={}, amount={}",
            event.orderId(), event.userId(), event.finalOrderPrice());
    }
}
