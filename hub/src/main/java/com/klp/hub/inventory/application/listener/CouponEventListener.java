package com.klp.hub.inventory.application.listener;

import com.klp.hub.inventory.application.InventoryFacade;
import com.klp.hub.inventory.application.InventoryService;
import com.klp.hub.inventory.application.OutboxService;
import com.klp.hub.inventory.domain.event.CouponCancelledEvent;
import com.klp.hub.inventory.domain.event.CouponUsedEvent;
import com.klp.hub.inventory.domain.event.CouponUsedFailedEvent;
import com.klp.hub.inventory.domain.event.InventoryReplenishedEvent;
import com.klp.hub.inventory.infrastructure.kafka.config.KafkaTopicConfig;
import com.klp.hub.inventory.infrastructure.kafka.producer.InventoryEventProducer;
import com.klp.hub.inventory.presentation.dto.response.InventoryDeductResponseForEvent;
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

public class CouponEventListener {

    private final InventoryFacade inventoryFacade;
    private final InventoryEventProducer inventoryEventProducer;
    private final OutboxService outboxService;
    private final InventoryService inventoryService;

    @KafkaListener(
        topics = KafkaTopicConfig.COUPON_USED_TOPIC,
        groupId = "inventory-service-group",
        containerFactory = "inventoryKafkaListenerContainerFactory"
    )
    @Transactional
    public void handleCouponUsed(
        @Payload CouponUsedEvent event,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment
    ) {
        log.info("쿠폰 사용 이벤트 수신: orderId={}, partition={}, offset={}",
            event.orderId(), partition, offset);

        try {
            inventoryFacade.confirm(event.orderId());

            InventoryDeductResponseForEvent response = inventoryService.deductWithEventPublishing(
                event);

            log.info("쿠폰 사용 이벤트 처리 완료: orderId={}, status={}",
                event.orderId(), response.status());

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.info("오프셋 커밋 완료: orderId={}, offset={}", event.orderId(), offset);
            }

        } catch (Exception e) {
            log.error("시스템 예외 발생: orderId={}, partition={}, offset={}, error={}",
                event.orderId(), partition, offset, e.getMessage(), e);
            throw e;
        }
    }

    @KafkaListener(
        topics = KafkaTopicConfig.COUPON_CANCELLED_TOPIC,
        groupId = "inventory-service-group",
        containerFactory = "inventoryKafkaListenerContainerFactory"
    )
    @Transactional
    public void handleCouponCancelled(
        @Payload CouponCancelledEvent event,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment
    ) {
        log.info("=== 쿠폰 사용 취소 이벤트 수신: orderId={}, userCouponId={}, partition={}, offset={} ===",
            event.orderId(), event.userCouponId(), partition, offset);

        try {
            InventoryReplenishedEvent inventoryReplenishedEvent =
                InventoryReplenishedEvent.of(event);
            // CouponUsedEvent를 아웃박스에 저장 (트랜잭션 내에서 저장)
//            inventoryEventProducer.publishInventoryReplenishedEvent(inventoryReplenishedEvent);
            outboxService.saveInventoryReplenishedEvent(inventoryReplenishedEvent);
            log.info("재고 복구 및 아웃박스 이벤트 저장 완료: orderId={}, userCouponId={}", event.orderId(),
                event.userCouponId());

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.info("오프셋 커밋 완료: orderId={}, offset={}", event.orderId(), offset);
            }
        } catch (Exception e) {
            log.error("결제 승인 이벤트 처리 실패: orderId={}, partition={}, offset={}",
                event.orderId(), partition, offset, e);
            throw e;
        }
    }

    @KafkaListener(
        topics = KafkaTopicConfig.COUPON_USED_FAILED_TOPIC,
        groupId = "inventory-service-group",
        containerFactory = "inventoryKafkaListenerContainerFactory"
    )
    public void handleCouponUsedFailed(CouponUsedFailedEvent event) {
        log.info("쿠폰 사용실패 이벤트 수신: orderId={}", event.orderId());
        inventoryFacade.release(event.orderId());
        log.info("재고 선점 해제 완료: orderId={}", event.orderId());
    }

    @KafkaListener(
        topics = KafkaTopicConfig.COUPON_USED_DLT,
        groupId = "inventory-service-group-dlt",
        containerFactory = "inventoryKafkaListenerContainerFactory"
    )
    public void handleCouponUsedDlt(@Payload CouponUsedEvent event) {
        log.error("========================================");
        log.error("⚠️ DLT 도착: CouponUsed");
        log.error("⚠️ 쿠폰 사용 처리 실패 - 수동 처리 필요!");
        log.error("========================================");
        log.error("orderId={}, userCouponId={}", event.orderId(), event.userCouponId());
    }

    @KafkaListener(
        topics = KafkaTopicConfig.COUPON_CANCELLED_DLT,
        groupId = "inventory-service-group-dlt",
        containerFactory = "inventoryKafkaListenerContainerFactory"
    )
    public void handleCouponCancelledDlt(@Payload CouponCancelledEvent event) {
        log.error("========================================");
        log.error("⚠️ DLT 도착: CouponCancelled");
        log.error("⚠️ 쿠폰 취소 처리 실패 - 수동 처리 필요!");
        log.error("========================================");
        log.error("orderId={}, userCouponId={}", event.orderId(), event.userCouponId());
    }

    @KafkaListener(
        topics = KafkaTopicConfig.COUPON_USED_FAILED_DLT,
        groupId = "inventory-service-group-dlt",
        containerFactory = "inventoryKafkaListenerContainerFactory"
    )
    public void handleCouponUsedFailedDlt(@Payload CouponUsedFailedEvent event) {
        log.error("========================================");
        log.error("⚠️ DLT 도착: CouponUsedFailed");
        log.error("⚠️ 쿠폰 사용 실패 처리 실패 - 수동 처리 필요!");
        log.error("========================================");
        log.error("orderId={}, userCouponId={}", event.orderId(), event.userCouponId());
    }

}
