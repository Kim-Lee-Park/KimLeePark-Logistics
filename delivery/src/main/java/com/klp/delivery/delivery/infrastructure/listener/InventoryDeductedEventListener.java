package com.klp.delivery.delivery.infrastructure.listener;

import com.klp.delivery.common.enums.IdempotencyStatus;
import com.klp.delivery.delivery.application.command.IdempotencyCommand;
import com.klp.delivery.delivery.application.command.OrderToDeliveryCommand;
import com.klp.delivery.delivery.application.command.OrderToDeliveryCommand.OrderItemCommand;
import com.klp.delivery.delivery.application.facade.DeliveryFacade;
import com.klp.delivery.delivery.domain.event.InventoryDeductedEvent;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryDeductedEventListener {

    private final DeliveryFacade deliveryFacade;

    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000L, multiplier = 2.0, maxDelay = 4000L),
        autoCreateTopics = "true",
        include = Exception.class,
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(
        topics = "inventory.topic",
        groupId = "delivery-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleInventoryDeducted(
        @Payload InventoryDeductedEvent event,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment) {

        log.info("=== 재고 차감 이벤트 수신: orderId={}, partition={}, offset={}, products={} ===",
            event.orderId(), partition, offset, event.products().size());

        try {
            // InventoryDeductedEvent에서 OrderItemCommand 생성
            List<OrderItemCommand> orderItemCommands = event.products().stream()
                .map(product -> new OrderItemCommand(
                    product.orderItemId(),
                    product.hubId(),
                    product.productName(),
                    product.quantity()
                ))
                .collect(Collectors.toList());

            // OrderToDeliveryCommand 생성 (InventoryDeductedEvent의 모든 정보 사용)
            // TODO: name과 comment 필드는 InventoryDeductedEvent에 추후 추가 예정
            OrderToDeliveryCommand orderCommand = new OrderToDeliveryCommand(
                event.orderId(),
                "", // TODO: 추후 InventoryDeductedEvent.name() 사용 예정
                event.email(),
                event.address(),
                event.userAddressHubId(),
                event.createdAt(),
                null, // TODO: 추후 InventoryDeductedEvent.comment() 사용 예정
                orderItemCommands
            );

            // IdempotencyCommand 생성 (deliveryIdempotencyKey 사용)
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

    @DltHandler
    public void handleInventoryDeductedDlt(
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

