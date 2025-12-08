package com.klp.payment.payment.infrastructure.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.payment.payment.domain.event.PaymentApprovedEvent;
import com.klp.payment.payment.domain.event.PaymentCancelledEvent;
import com.klp.payment.payment.domain.outbox.PaymentOutbox;
import com.klp.payment.payment.domain.repository.PaymentOutboxRepository;
import com.klp.payment.payment.infrastructure.kafka.producer.PaymentEventProducer;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxScheduler {

    private final PaymentOutboxRepository paymentOutboxRepository;
    private final PaymentEventProducer eventProducer;
    private final ObjectMapper objectMapper;

    private static final int BATCH_SIZE = 100;

    @Scheduled(fixedDelay = 1000)
    public void publishPendingEvents() {
        List<PaymentOutbox> pendingEvents = paymentOutboxRepository.findPendingEvents(BATCH_SIZE);

        for (PaymentOutbox outbox : pendingEvents) {
            try {
                publishEvent(outbox);
                paymentOutboxRepository.markAsPublished(outbox.getPaymentOutboxId());
                log.info("이벤트 발행 성공: orderId={}, eventType={}", outbox.getOrderId(), outbox.getEventType());
            } catch (Exception e) {
                paymentOutboxRepository.markAsFailed(outbox.getPaymentOutboxId());
                log.error("이벤트 발행 실패: orderId={}, error={}", outbox.getOrderId(), e.getMessage());
            }
        }
    }

    private void publishEvent(PaymentOutbox outbox) throws Exception {
        String eventType = outbox.getEventType();
        String payload = outbox.getPayload();

        switch (eventType) {
            case "PaymentApprovedEvent" -> {
                PaymentApprovedEvent event = objectMapper.readValue(payload, PaymentApprovedEvent.class);
                eventProducer.publishPaymentApprovedEvent(event);
            }
            case "PaymentCancelledEvent" -> {
                PaymentCancelledEvent event = objectMapper.readValue(payload, PaymentCancelledEvent.class);
                eventProducer.publishPaymentCancelledEvent(event);
            }
            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        }
    }
}
