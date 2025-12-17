package com.klp.payment.payment.application.listener;

import com.klp.payment.payment.application.PaymentService;
import com.klp.payment.payment.domain.entity.Payment;
import com.klp.payment.payment.domain.event.InventoryDeductedEvent;
import com.klp.payment.payment.domain.event.InventoryDeductedFailedEvent;
import com.klp.payment.payment.infrastructure.kafka.config.KafkaTopicConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@KafkaListener(
    topics = KafkaTopicConfig.INVENTORY_TOPIC,
    groupId = "payment-service-group",
    containerFactory = "paymentKafkaListenerContainerFactory"
)
public class InventoryEventListener {

    private final PaymentService paymentService;

    @KafkaHandler
    @Transactional
    public void handleInventoryFailed(@Payload InventoryDeductedFailedEvent event) {
        log.info("재고 차감 실패 이벤트 수신: orderId={}", event.orderId());

        Payment payment = paymentService.failPayment(event.orderId(), "결제취소");

        log.warn("재고 완료 : orderId={}, paymentId={}, reason={}", event.orderId(),
            payment.getPaymentId(), payment.getReason());
    }

    @KafkaHandler
    @Transactional
    public void handleInventoryDeducted(@Payload InventoryDeductedEvent event) {
        log.info("재고 차감  이벤트 수신: orderId={}", event.orderId());
    }


    @KafkaHandler(isDefault = true)
    public void handleUnknown(Object event) {
        log.warn("알 수 없는 이벤트 타입 수신: {}", event.getClass().getSimpleName());
    }
}
