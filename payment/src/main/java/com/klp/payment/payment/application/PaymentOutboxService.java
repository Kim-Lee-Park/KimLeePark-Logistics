package com.klp.payment.payment.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.payment.payment.domain.event.PaymentApprovedEvent;
import com.klp.payment.payment.domain.event.PaymentCancelledEvent;
import com.klp.payment.payment.domain.outbox.PaymentOutbox;
import com.klp.payment.payment.domain.repository.PaymentOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentOutboxService {

    private final PaymentOutboxRepository paymentOutboxRepository;
    private final ObjectMapper objectMapper;

    public void savePaymentApprovedEvent(PaymentApprovedEvent event) {
        saveOutbox(event.orderId(), "PaymentApprovedEvent", event);
    }

    public void savePaymentCancelledEvent(PaymentCancelledEvent event) {
        saveOutbox(event.orderId(), "PaymentCancelledEvent", event);
    }

    private void saveOutbox(java.util.UUID orderId, String eventType, Object event) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            PaymentOutbox outbox = PaymentOutbox.create(orderId, eventType, payload);

            paymentOutboxRepository.save(outbox);
            log.info("Outbox 저장 완료: orderId={}, eventType={}", orderId, eventType);

        } catch (JsonProcessingException e) {
            log.error("이벤트 직렬화 실패: orderId={}", orderId, e);
            throw new RuntimeException("이벤트 직렬화 실패", e);
        }
    }

}
