package com.klp.order.infrastructure.event.listener;

import com.klp.order.application.service.OrderService;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.domain.entity.order.OrderStatus;
import com.klp.order.domain.repository.OrderRepository;
import com.klp.order.infrastructure.event.event.PaymentApprovedEvent;
import com.klp.order.infrastructure.event.event.PaymentCancelledEvent;
import com.klp.order.infrastructure.event.event.PaymentCancelledFailedEvent;
import com.klp.order.infrastructure.event.event.PaymentFailedEvent;
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
public class PaymentEventListener {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    @KafkaListener(
        topics = "payment.approved",
        groupId = "order-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
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

    @KafkaListener(
        topics = "payment.failed",
        groupId = "order-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handlePaymentFailed(
        @Payload PaymentFailedEvent event,
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

    @KafkaListener(
        topics = "payment.cancelled",
        groupId = "order-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handlePaymentCancelled(
        @Payload PaymentCancelledEvent event,
        Acknowledgment acknowledgment) {

        log.info("=== 결제 취소 완료 이벤트 수신: orderId={} ===", event.orderId());

        try {
            Order order = orderService.findById(event.orderId());

            // 주문은 이미 CANCELLED 상태여야 함
            if (order.getOrderStatus() != OrderStatus.CANCELLED) {
                log.warn("주문이 취소 상태가 아닌데 결제 취소 이벤트 수신: orderId={}, status={}",
                    event.orderId(), order.getOrderStatus());
            }

            log.info("결제 취소 확인 완료: orderId={}", event.orderId());

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }

        } catch (Exception e) {
            log.error("결제 취소 이벤트 처리 실패: orderId={}", event.orderId(), e);
            throw e;
        }
    }

    @KafkaListener(
        topics = "payment.cancelled.failed",
        groupId = "order-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handlePaymentCancelledFailed(
        @Payload PaymentCancelledFailedEvent event,
        Acknowledgment acknowledgment) {

        log.error("=== 결제 취소 실패 이벤트 수신: orderId={} ===", event.orderId());

        try {
            Order order = orderService.findById(event.orderId());

            // 결제 취소도 실패했다면 수동 개입 필요
            // OrderStatus.PAYMENT_CANCEL_FAILED 같은 상태 추가 고려

            log.error("⚠️ 결제 취소 실패 - 수동 처리 필요: orderId={}",
                event.orderId());

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }

        } catch (Exception e) {
            log.error("결제 취소 실패 이벤트 처리 중 오류: orderId={}", event.orderId(), e);
            throw e;
        }
    }


    @KafkaListener(
        topics = "payment.approved.order.dlt",
        groupId = "order-service-group-dlt",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentApprovedDlt(PaymentApprovedEvent event) {
        log.error("========================================");
        log.error("⚠️ DLT 도착: PaymentApproved");
        log.error("⚠️ 결제 완료 처리 실패 - 수동 처리 필요!");
        log.error("========================================");
        log.error("orderId={}, paymentId={}", event.orderId(), event.paymentId());
    }

    @KafkaListener(
        topics = "payment.failed.order.dlt",
        groupId = "order-service-group-dlt",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentFailedDlt(PaymentFailedEvent event) {
        log.error("========================================");
        log.error("⚠️ DLT 도착: PaymentFailed");
        log.error("⚠️ 결제 실패 처리 실패 - 수동 처리 필요!");
        log.error("========================================");
        log.error("orderId={}, reason={}", event.orderId(), event.reason());
    }

    @KafkaListener(
        topics = "payment.cancelled.order.dlt",
        groupId = "order-service-group-dlt",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentCancelledDlt(PaymentCancelledEvent event) {
        log.error("========================================");
        log.error("⚠️ DLT 도착: PaymentCancelled");
        log.error("⚠️ 결제 취소 확인 실패 - 수동 처리 필요!");
        log.error("========================================");
        log.error("orderId={}", event.orderId());
    }

    @KafkaListener(
        topics = "payment.cancelled.failed.order.dlt",
        groupId = "order-service-group-dlt",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentCancelledFailedDlt(PaymentCancelledFailedEvent event) {
        log.error("========================================");
        log.error("⚠️ DLT 도착: PaymentCancelledFailed");
        log.error("⚠️ 결제 취소 실패 처리 실패 - 수동 처리 필요!");
        log.error("========================================");
        log.error("orderId={}", event.orderId());
    }
}
