package com.klp.order.infrastructure.event.listener;

import com.klp.order.application.service.OrderService;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.domain.entity.order.OrderStatus;
import com.klp.order.domain.repository.OrderRepository;
import com.klp.order.infrastructure.event.event.DeliveryCreatedFailedEvent;
import com.klp.order.infrastructure.event.event.DeliveryShippingFailedEvent;
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
public class DeliveryShippingFailedEventListener {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000L, multiplier = 2.0, maxDelay = 4000L),
        autoCreateTopics = "true",
        include = Exception.class,
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(
        topics = "delivery.topic",
        groupId = "order-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleDeliveryShippingFailed(
        @Payload DeliveryShippingFailedEvent event,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment) {

        log.info("=== 배송 중 실패 이벤트 수신: orderId={}, partition={}, offset={}, items={} ===",
            event.orderId(), partition, offset, event.items().size());

        try {
            // 1. 주문 조회
            Order order = orderService.findById(event.orderId());
            log.info("주문 조회 완료 - orderId: {}, 현재 상태: {}",
                order.getOrderId(), order.getOrderStatus());

            if (order.getOrderStatus() == OrderStatus.DELIVERY_SHIPPING_FAILED ||
                order.getOrderStatus().equals(OrderStatus.FAILED)
            ) {
                log.info("이미 처리된 배송 중 실패 이벤트 - orderId: {}", event.orderId());
                if (acknowledgment != null) {
                    acknowledgment.acknowledge();
                }
                return;
            }
            order.changeStatus(OrderStatus.DELIVERY_SHIPPING_FAILED);
            orderRepository.save(order);

            // 4. 수동 커밋
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.info("오프셋 커밋 완료: orderId={}, offset={}", event.orderId(), offset);
            }

            log.info("=== 배송 중 실패 이벤트 처리 완료: orderId={}, 상태={} ===",
                event.orderId(), OrderStatus.DELIVERY_CREATED_FAILED);

        } catch (Exception e) {
            log.error("배송 중 실패 이벤트 처리 실패: orderId={}, partition={}, offset={}",
                event.orderId(), partition, offset, e);

            // 이벤트 처리 실패 시 재시도를 위해 예외를 다시 던짐
            // DefaultErrorHandler가 재시도 처리
            throw e;
        }
    }

    // 실패시 자동으로 호출한다고 합니다.
    @DltHandler
    public void handleDeliveryCreatedFailedDlt(
        @Payload DeliveryCreatedFailedEvent event,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        @Header(KafkaHeaders.EXCEPTION_MESSAGE) String exceptionMessage) {

        log.error("========================================");
        log.error("⚠️ DLT 도착: Delivery Shipping Failed Event");
        log.error("⚠️ 수동 처리가 필요합니다!");
        log.error("========================================");
        log.error("orderId={}, error={}", event.orderId(), exceptionMessage);
    }
}
