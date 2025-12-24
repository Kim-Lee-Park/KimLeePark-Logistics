package com.klp.payment.payment.application.listener;

import com.klp.payment.payment.application.PaymentService;
import com.klp.payment.payment.domain.entity.Payment;
import com.klp.payment.payment.domain.event.InventoryDeductedFailedEvent;
import com.klp.payment.payment.infrastructure.kafka.config.KafkaTopicConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventListener {

    private final PaymentService paymentService;

    @KafkaListener(
        topics = KafkaTopicConfig.INVENTORY_DEDUCTED_FAILED_TOPIC,
        groupId = "payment-service-group",
        containerFactory = "paymentKafkaListenerContainerFactory"
    )
    @Transactional
    public void handleInventoryFailed(@Payload InventoryDeductedFailedEvent event) {
        log.info("재고 차감 실패 이벤트 수신: orderId={}", event.orderId());

        Payment payment = paymentService.failPayment(event.orderId(), "결제취소");

        log.warn("재고 완료 : orderId={}, paymentId={}, reason={}", event.orderId(),
            payment.getPaymentId(), payment.getReason());
    }

    @KafkaListener(
        topics = KafkaTopicConfig.INVENTORY_DEDUCTED_FAILED_DLT,
        groupId = "payment-service-group-dlt",
        containerFactory = "paymentKafkaListenerContainerFactory"
    )
    public void handleInventoryDeductedFailedDlt(@Payload InventoryDeductedFailedEvent event) {
        log.error("========================================");
        log.error("⚠️ DLT 도착: InventoryDeductedFailedEvent");
        log.error("⚠️ 3회 재시도 후에도 실패했습니다!");
        log.error("⚠️ 수동 처리가 필요합니다!");
        log.error("========================================");
        log.error("orderId={}, userId={}, amount={}",
            event.orderId(), event.userId(), event.finalOrderPrice());
    }
}
