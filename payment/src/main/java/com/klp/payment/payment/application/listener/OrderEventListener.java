package com.klp.payment.payment.application.listener;

import com.klp.payment.payment.application.PaymentOutboxService;
import com.klp.payment.payment.application.PaymentService;
import com.klp.payment.payment.domain.entity.Payment;
import com.klp.payment.payment.domain.event.OrderCancelledEvent;
import com.klp.payment.payment.domain.event.OrderCreatedEvent;
import com.klp.payment.payment.domain.event.OrderFailedEvent;
import com.klp.payment.payment.domain.event.PaymentApprovedEvent;
import com.klp.payment.payment.domain.event.PaymentCancelledEvent;
import com.klp.payment.payment.domain.event.PaymentFailedEvent;
import com.klp.payment.payment.infrastructure.kafka.config.KafkaTopicConfig;
import java.util.UUID;
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
public class OrderEventListener {

    private final PaymentService paymentService;
    private final PaymentOutboxService outboxService;

    @KafkaListener(
        topics = KafkaTopicConfig.ORDER_CREATED_TOPIC,
        groupId = "payment-service-group",
        containerFactory = "paymentKafkaListenerContainerFactory"
    )
    @Transactional
    public void handleOrderCreated(
        @Payload OrderCreatedEvent event,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment) {

        log.info("=== 주문 생성 이벤트 수신: orderId={}, amount={}, partition={}, offset={} ===",
            event.orderId(), event.finalOrderPrice(), partition, offset);

        try {
            if (paymentService.existsByOrderId(event.orderId())) {
                log.info("이미 처리된 주문입니다: orderId={}", event.orderId());
                if (acknowledgment != null) {
                    acknowledgment.acknowledge();
                }
                return;
            }
            UUID hubId = event.products().isEmpty() ? null : event.products().get(0).hubId();
            Payment payment = paymentService.processPayment(
                event.orderId(),
                event.userId(),
                hubId,
                (long) event.finalOrderPrice()
            );

            if (payment.isApproved()) {
                PaymentApprovedEvent approvedEvent = PaymentApprovedEvent.from(
                    payment.getPaymentId(),
                    payment.getAmount().intValue(),
                    payment.getMethod().name(),
                    payment.getPaidAt(),
                    event
                );

                outboxService.savePaymentApprovedEvent(approvedEvent);
                log.info("결제 승인 완료: paymentId={}, orderId={}",
                    payment.getPaymentId(), event.orderId());
            } else {
                PaymentFailedEvent failedEvent = PaymentFailedEvent.from(
                    payment.getPaymentId(),
                    event.orderId(),
                    event.userId(),
                    event.userCouponId(),
                    payment.getReason(),
                    event
                );

                outboxService.savePaymentFailedEvent(failedEvent);
                log.warn("결제 실패: orderId={}, reason={}",
                    event.orderId(), payment.getReason());
            }

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.info("오프셋 커밋 완료: orderId={}, offset={}", event.orderId(), offset);
            }

        } catch (Exception e) {
            log.error("주문 생성 이벤트 처리 실패: orderId={}, partition={}, offset={}",
                event.orderId(), partition, offset, e);
            // 예외 발생 시 커밋 안 함 -> 재시도 -> 실패 시 DLT
            throw e;
        }
    }

    @KafkaListener(
        topics = KafkaTopicConfig.ORDER_CANCELLED_TOPIC,
        groupId = "payment-service-group",
        containerFactory = "paymentKafkaListenerContainerFactory"
    )
    @Transactional
    public void handleOrderCancelled(
        @Payload OrderCancelledEvent event,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment) {

        log.info("=== 주문 취소 이벤트 수신: orderId={}, partition={}, offset={} ===",
            event.orderId(), partition, offset);

        try {
            Payment payment = paymentService.cancelPaymentByOrderId(event.orderId(), "주문 취소");

            if (payment != null) {
                PaymentCancelledEvent cancelledEvent = PaymentCancelledEvent.from(
                    payment.getPaymentId(),
                    event.orderId(),
                    event.userId(),
                    event.userCouponId(),
                    event.inventoryIdempotencyKey(),
                    event.deliveryIdempotencyKey(),
                    event.cancelReason(),
                    event.products().stream()
                        .map(p -> new PaymentCancelledEvent.ProductInfo(
                            p.productId(),
                            p.hubId(),
                            p.quantity()
                        ))
                        .toList(),
                    event.cancelledAt()
                );

                outboxService.savePaymentCancelledEvent(cancelledEvent);
                log.info("결제 취소 완료: paymentId={}, orderId={}",
                    payment.getPaymentId(), event.orderId());
            } else {
                log.info("취소할 결제가 없습니다: orderId={}", event.orderId());
            }

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.info("오프셋 커밋 완료: orderId={}, offset={}", event.orderId(), offset);
            }

        } catch (Exception e) {
            log.error("주문 취소 이벤트 처리 실패: orderId={}, partition={}, offset={}",
                event.orderId(), partition, offset, e);
            // 예외 발생 시 커밋 안 함 -> 재시도 -> 실패 시 DLT
            throw e;
        }
    }

    @KafkaListener(
        topics = KafkaTopicConfig.ORDER_CREATED_DLT,
        groupId = "payment-service-group-dlt",
        containerFactory = "paymentKafkaListenerContainerFactory"
    )
    public void handleOrderCreatedDlt(@Payload OrderCreatedEvent event) {
        log.error("========================================");
        log.error("⚠️ DLT 도착: OrderCreated");
        log.error("⚠️ 3회 재시도 후에도 실패했습니다!");
        log.error("⚠️ 수동 처리가 필요합니다!");
        log.error("========================================");
        log.error("orderId={}, userId={}, amount={}",
            event.orderId(), event.userId(), event.finalOrderPrice());
    }

    @KafkaListener(
        topics = KafkaTopicConfig.ORDER_CANCELLED_DLT,
        groupId = "payment-service-group-dlt",
        containerFactory = "paymentKafkaListenerContainerFactory"
    )
    public void handleOrderCancelledDlt(@Payload OrderCancelledEvent event) {
        log.error("========================================");
        log.error("⚠️ DLT 도착: OrderCancelled");
        log.error("⚠️ 보상 트랜잭션이 실패했습니다!");
        log.error("⚠️ 수동 처리가 필요합니다!");
        log.error("========================================");
        log.error("orderId={}", event.orderId());

    }
}