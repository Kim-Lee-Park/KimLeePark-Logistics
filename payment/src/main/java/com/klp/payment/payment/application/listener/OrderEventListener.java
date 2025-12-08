package com.klp.payment.payment.application.listener;

import com.klp.payment.payment.application.PaymentOutboxService;
import com.klp.payment.payment.application.PaymentService;
import com.klp.payment.payment.domain.entity.Payment;
import com.klp.payment.payment.domain.event.OrderCancelledEvent;
import com.klp.payment.payment.domain.event.OrderCreatedEvent;
import com.klp.payment.payment.domain.event.PaymentApprovedEvent;
import com.klp.payment.payment.domain.event.PaymentCancelledEvent;
import com.klp.payment.payment.domain.event.PaymentFailedEvent;
import com.klp.payment.payment.infrastructure.kafka.config.KafkaTopicConfig;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@KafkaListener(
    topics = KafkaTopicConfig.ORDER_TOPIC,
    groupId = "payment-service-group",
    containerFactory = "paymentKafkaListenerContainerFactory"
)
public class OrderEventListener {

    private final PaymentService paymentService;
    private final PaymentOutboxService outboxService;

    @KafkaHandler
    @Transactional
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("주문 생성 이벤트 수신: orderId={}, amount={}", event.orderId(), event.finalOrderPrice());

        if (paymentService.existsByOrderId(event.orderId())) {
            log.info("이미 처리된 주문입니다: orderId={}", event.orderId());
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
            String couponIdempotencyKey = UUID.randomUUID().toString();

            PaymentApprovedEvent approvedEvent = PaymentApprovedEvent.from(
                payment.getPaymentId(),
                payment.getAmount().intValue(),
                payment.getMethod().name(),
                payment.getPaidAt(),
                event,
                couponIdempotencyKey
            );

            outboxService.savePaymentApprovedEvent(approvedEvent);
            log.info("결제 승인 완료: paymentId={}", payment.getPaymentId());
        } else {
            PaymentFailedEvent failedEvent = PaymentFailedEvent.from(
                payment.getPaymentId(),
                event.orderId(),
                event.userId(),
                payment.getReason(),
                event
            );

            outboxService.savePaymentFailedEvent(failedEvent);
            log.warn("결제 실패: orderId={}, reason={}", event.orderId(), payment.getReason());
        }
    }

    @KafkaHandler
    @Transactional
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("주문 취소 이벤트 수신: orderId={}", event.orderId());

        Payment payment = paymentService.cancelPaymentByOrderId(event.orderId(), "주문 취소");

        if (payment != null) {
            PaymentCancelledEvent cancelledEvent = PaymentCancelledEvent.from(
                payment.getPaymentId(),
                event.orderId(),
                payment.getUserId(),
                "주문 취소",
                event.products().stream()
                    .map(p -> new PaymentCancelledEvent.ProductInfo(
                        p.productId(),
                        p.hubId(),
                        p.quantity()
                    ))
                    .toList()
            );

            outboxService.savePaymentCancelledEvent(cancelledEvent);
            log.info("결제 취소 완료: paymentId={}", payment.getPaymentId());
        } else {
            log.info("취소할 결제가 없습니다: orderId={}", event.orderId());
        }
    }

    @KafkaHandler(isDefault = true)
    public void handleUnknown(Object event) {
        log.warn("알 수 없는 이벤트 타입 수신: {}", event.getClass().getSimpleName());
    }
}
