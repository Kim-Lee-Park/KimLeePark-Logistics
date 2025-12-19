package com.klp.payment.payment.application.listener;

import com.klp.payment.payment.application.PaymentOutboxService;
import com.klp.payment.payment.application.PaymentService;
import com.klp.payment.payment.domain.entity.Payment;
import com.klp.payment.payment.domain.event.CouponUsedFailedEvent;
import com.klp.payment.payment.infrastructure.kafka.config.KafkaTopicConfig;
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

    private final PaymentService paymentService;
    private final PaymentOutboxService outboxService;

    @KafkaListener(
        topics = KafkaTopicConfig.COUPON_USED_FAILED_TOPIC,
        groupId = "payment-service-group",
        containerFactory = "paymentKafkaListenerContainerFactory"
    )
    @Transactional
    public void handleCouponUsedFailed(
        @Payload CouponUsedFailedEvent event,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment) {

        log.info("=== 쿠폰 사용 실패 이벤트 수신: orderId={}, userCouponId={}, partition={}, offset={} ===",
            event.orderId(), event.userCouponId(), partition, offset);

        try {
            Payment payment = paymentService.failPayment(event.orderId(), "쿠폰 사용 실패");

            log.warn("결제 실패 완료: orderId={}, paymentId={}, reason={}",
                event.orderId(), payment.getPaymentId(), payment.getReason());

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.info("오프셋 커밋 완료: orderId={}, offset={}", event.orderId(), offset);
            }

        } catch (Exception e) {
            log.error("쿠폰 사용 실패 이벤트 처리 중 오류: orderId={}, partition={}, offset={}",
                event.orderId(), partition, offset, e);
            throw e;
        }
    }

    @KafkaListener(
        topics = KafkaTopicConfig.COUPON_USED_FAILED_DLT,
        groupId = "payment-service-group-dlt",
        containerFactory = "paymentKafkaListenerContainerFactory"
    )
    public void handleCouponUsedFailedDlt(@Payload CouponUsedFailedEvent event) {
        log.error("========================================");
        log.error("⚠️ DLT 도착: CouponUsedFailed");
        log.error("⚠️ 쿠폰 사용 실패 처리 실패 - 수동 처리 필요!");
        log.error("========================================");
        log.error("orderId={}, userCouponId={}", event.orderId(), event.userCouponId());
    }
}