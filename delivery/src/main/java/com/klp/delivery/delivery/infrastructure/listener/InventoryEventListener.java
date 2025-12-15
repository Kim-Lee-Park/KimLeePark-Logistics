package com.klp.delivery.delivery.infrastructure.listener;

import com.klp.delivery.common.enums.IdempotencyStatus;
import com.klp.delivery.delivery.application.command.IdempotencyCommand;
import com.klp.delivery.delivery.application.command.OrderToDeliveryCommand;
import com.klp.delivery.delivery.application.facade.DeliveryFacade;
import com.klp.delivery.delivery.domain.event.InventoryDeductedEvent;
import com.klp.delivery.delivery.domain.event.InventoryReplenishedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaHandler;
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
@KafkaListener(
    topics = "inventory.topic",
    groupId = "delivery-service-group",
    containerFactory = "kafkaListenerContainerFactory"
)
public class InventoryEventListener {

    private final DeliveryFacade deliveryFacade;

    @KafkaHandler
    @Transactional
    public void handleInventoryDeducted(
        @Payload InventoryDeductedEvent event,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment) {

        log.info("=== 재고 차감 이벤트 수신: orderId={}, partition={}, offset={}, products={} ===",
            event.orderId(), partition, offset, event.products().size());

        try {
            OrderToDeliveryCommand orderCommand = OrderToDeliveryCommand.from(event);

            IdempotencyCommand idempotencyCommand = new IdempotencyCommand(
                event.deliveryIdempotencyKey(),
                event.orderId(),
                IdempotencyStatus.PENDING
            );

            // 배송 생성
            deliveryFacade.createDelivery(orderCommand, idempotencyCommand);
            log.info("=== 배송 생성 완료: orderId={} ===", event.orderId());

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.info("오프셋 커밋 완료: orderId={}, offset={}", event.orderId(), offset);
            }

        } catch (Exception e) {
            log.error("재고 차감 이벤트 처리 실패: orderId={}, partition={}, offset={}",
                event.orderId(), partition, offset, e);
            throw e;
        }
    }

    @KafkaHandler
    @Transactional
    public void handleInventoryReplenished(
        @Payload InventoryReplenishedEvent event,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment) {

        log.info("=== 재고 복구 이벤트 수신: orderId={}, partition={}, offset={}, cancelReason={} ===",
            event.orderId(), partition, offset, event.cancelReason());

        try {
            Long deletedBy = event.userId() != null ? event.userId() : 0L;
            deliveryFacade.cancelDeliveriesByOrderId(event.orderId(), deletedBy);
            log.info("=== 배송 취소 완료: orderId={} ===", event.orderId());

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.info("오프셋 커밋 완료: orderId={}, offset={}", event.orderId(), offset);
            }

        } catch (Exception e) {
            log.error("재고 복구 이벤트 처리 실패: orderId={}, partition={}, offset={}",
                event.orderId(), partition, offset, e);
            throw e;
        }
    }

    @DltHandler
    public void handleInventorydDlt(
        @Payload InventoryDeductedEvent event,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        @Header(KafkaHeaders.EXCEPTION_MESSAGE) String exceptionMessage) {

        log.error("========================================");
        log.error("⚠️ DLT 도착: Inventory Deducted Event");
        log.error("⚠️ 수동 처리가 필요합니다!");
        log.error("========================================");
        log.error("orderId={}, error={}", event.orderId(), exceptionMessage);
    }
}

