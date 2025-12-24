package com.klp.payment.payment.infrastructure.kafka.producer;

import com.klp.payment.payment.domain.event.PaymentApprovedEvent;
import com.klp.payment.payment.domain.event.PaymentCancelledEvent;
import com.klp.payment.payment.domain.event.PaymentFailedEvent;
import com.klp.payment.payment.infrastructure.kafka.config.KafkaTopicConfig;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentEventProducer(
        @Qualifier("paymentKafkaTemplate") KafkaTemplate<String, Object> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * 결제 승인 이벤트 발행
     */
    public void publishPaymentApprovedEvent(PaymentApprovedEvent event) {
        String key = event.orderId().toString();

        CompletableFuture<SendResult<String, Object>> future =
            kafkaTemplate.send(KafkaTopicConfig.PAYMENT_APPROVED_TOPIC, key, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("결제 승인 이벤트 발행 성공: orderId={}, paymentId={}", event.orderId(),
                    event.paymentId());
            } else {
                log.error("결제 승인 이벤트 발행 실패: orderId={}, paymentId={}, error={}", event.orderId(),
                    event.paymentId(),
                    ex.getMessage());
            }
        });
    }

    /**
     * 결제 취소 이벤트 발행
     */
    public void publishPaymentCancelledEvent(PaymentCancelledEvent event) {
        String key = event.orderId().toString();

        CompletableFuture<SendResult<String, Object>> future =
            kafkaTemplate.send(KafkaTopicConfig.PAYMENT_CANCELLED_TOPIC, key, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("결제 취소 이벤트 발행 성공: orderId={}, paymentId={}", event.orderId(),
                    event.paymentId());
            } else {
                log.error("결제 취소 이벤트 발행 실패: orderId={}, paymentId={}, error={}", event.orderId(),
                    event.paymentId(),
                    ex.getMessage());
            }
        });
    }

    /**
     * 결제 실패 이벤트 발행
     */
    public void publishPaymentFailedEvent(PaymentFailedEvent event) {
        String key = event.orderId().toString();

        CompletableFuture<SendResult<String, Object>> future =
            kafkaTemplate.send(KafkaTopicConfig.PAYMENT_FAILED_TOPIC, key, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("결제 실패 이벤트 발행 성공: orderId={}, reason={}", event.orderId(), event.reason());
            } else {
                log.error("결제 실패 이벤트 발행 실패: orderId={}, error={}", event.orderId(),
                    ex.getMessage());
            }
        });
    }
}
