package com.klp.payment.payment.application.listener;

import com.klp.payment.payment.application.PaymentOutboxService;
import com.klp.payment.payment.application.PaymentService;
import com.klp.payment.payment.domain.entity.Payment;
import com.klp.payment.payment.domain.event.CouponUsedFailedEvent;
import com.klp.payment.payment.domain.event.PaymentFailedEvent;
import com.klp.payment.payment.infrastructure.kafka.config.KafkaTopicConfig;
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
    topics = KafkaTopicConfig.COUPON_TOPIC,
    groupId = "payment-service-group",
    containerFactory = "paymentKafkaListenerContainerFactory"
)
public class CouponEventListener {

    private final PaymentService paymentService;
    private final PaymentOutboxService outboxService;

    @KafkaHandler
    @Transactional
    public void handleOrderCreated(CouponUsedFailedEvent event) {
        log.info("쿠폰 사용 실패 이벤트 수신: orderId={}, userCouponId={}", event.orderId(), event.userCouponId());

        Payment payment = paymentService.failPayment(event.orderId(), "결제취소");

        PaymentFailedEvent failedEvent = PaymentFailedEvent.from(
            payment.getPaymentId(),
            event.orderId(),
            event.userId(),
            payment.getReason(),
            event
        );

        outboxService.savePaymentFailedEvent(failedEvent);
        log.warn("결제 실패 완료 : orderId={}, paymentId={}, reason={}", event.orderId(), payment.getPaymentId(), payment.getReason());
    }



    @KafkaHandler(isDefault = true)
    public void handleUnknown(Object event) {
        log.warn("알 수 없는 이벤트 타입 수신: {}", event.getClass().getSimpleName());
    }
}
