package com.klp.order.order.infrastructure.event.listener;

import com.klp.order.order.application.service.OrderService;
import com.klp.order.order.domain.entity.order.Order;
import com.klp.order.order.domain.entity.order.OrderStatus;
import com.klp.order.order.domain.repository.OrderRepository;
import com.klp.order.order.infrastructure.event.event.PaymentApprovedEvent;
import com.klp.order.order.infrastructure.event.event.PaymentApprovedFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaHandler;
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
@RetryableTopic(
    attempts = "3",
    backoff = @Backoff(delay = 1000L, multiplier = 2.0, maxDelay = 4000L),
    autoCreateTopics = "true",
    include = Exception.class,
    topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
)
@KafkaListener(
    topics = "payment.topic",
    groupId = "order-service-group",
    containerFactory = "kafkaListenerContainerFactory"
)
public class PaymentEventListener {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    @KafkaHandler
    @Transactional
    public void handlePaymentApproved(
        @Payload PaymentApprovedEvent event,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment) {

        log.info("=== 결제 완료 이벤트 수신: orderId={}, partition={}, offset={} ===",
            event.orderId(), partition, offset);

        try {
            Order order = orderService.findById(event.orderId());
            log.info("주문 조회 완료 - orderId: {}", order.getOrderId());

            if (order.getOrderStatus() == OrderStatus.PAID) {
                log.info("이미 처리된 결제 완료 이벤트 - orderId: {}", event.orderId());
                if (acknowledgment != null) {
                    acknowledgment.acknowledge();
                }
                return;
            }

            order.changeStatus(OrderStatus.PAID);
            orderRepository.save(order);
            log.info("=== 결제 완료 이벤트 처리 완료: orderId={} ===", event.orderId());

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.info("오프셋 커밋 완료: orderId={}, offset={}", event.orderId(), offset);
            }

        } catch (Exception e) {
            log.error("결제 완료 이벤트 처리 실패: orderId={}, partition={}, offset={}",
                event.orderId(), partition, offset, e);
            throw e;
        }
    }

    @KafkaHandler
    @Transactional
    public void handlePaymentApprovedFailed(
        @Payload PaymentApprovedFailedEvent event,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment) {

        log.info("=== 결제 실패 이벤트 수신: orderId={}, partition={}, offset={} ===",
            event.orderId(), partition, offset);

        try {
            Order order = orderService.findById(event.orderId());
            log.info("주문 조회 완료 - orderId: {}", order.getOrderId());

            if (order.getOrderStatus() == OrderStatus.FAILED
                || order.getOrderStatus() == OrderStatus.PAID_FAILED) {
                log.info("이미 처리된 결제 실패 이벤트 - orderId: {}", event.orderId());
                if (acknowledgment != null) {
                    acknowledgment.acknowledge();
                }
                return;
            }

            order.changeStatus(OrderStatus.PAID_FAILED);
            orderRepository.save(order);
            log.info("=== 결제 실패 이벤트 처리 완료: orderId={} ===", event.orderId());

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.info("오프셋 커밋 완료: orderId={}, offset={}", event.orderId(), offset);
            }

        } catch (Exception e) {
            log.error("결제 실패 이벤트 처리 실패: orderId={}, partition={}, offset={}",
                event.orderId(), partition, offset, e);
            throw e;
        }
    }

    @KafkaHandler(isDefault = true)
    public void handleUnknown(Object event) {
        log.warn("알 수 없는 이벤트 타입 수신: {}", event.getClass().getSimpleName());
    }

    @DltHandler
    public void handlePaymentDlt(
        @Payload Object event,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        @Header(KafkaHeaders.EXCEPTION_MESSAGE) String exceptionMessage) {

        log.error("========================================");
        log.error("⚠️ DLT 도착: Payment Event");
        log.error("⚠️ 수동 처리가 필요합니다!");
        log.error("========================================");
        log.error("eventType={}, error={}", event.getClass().getSimpleName(), exceptionMessage);
    }
}
