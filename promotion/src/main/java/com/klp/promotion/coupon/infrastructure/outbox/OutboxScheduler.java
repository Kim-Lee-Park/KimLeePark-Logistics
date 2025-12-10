package com.klp.promotion.coupon.infrastructure.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.promotion.coupon.domain.entity.outbox.CouponOutboxEvent;
import com.klp.promotion.coupon.domain.event.CouponUsedFailedEvent;
import com.klp.promotion.coupon.domain.event.CouponUsedEvent;
import com.klp.promotion.coupon.domain.repository.CouponOutboxEventRepository;
import com.klp.promotion.coupon.infrastructure.kafka.producer.CouponEventProducer;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxScheduler {

    private final CouponOutboxEventRepository couponOutboxEventRepository;
    private final CouponEventProducer eventProducer;
    private final ObjectMapper objectMapper;

    private static final int BATCH_SIZE = 100;

    @Scheduled(fixedDelay = 1000)
    public void publishPendingEvents() {
        List<CouponOutboxEvent> pendingEvents = couponOutboxEventRepository.findPendingEvents(BATCH_SIZE);

        for (CouponOutboxEvent outbox : pendingEvents) {
            try {
                publishEvent(outbox);
                couponOutboxEventRepository.markAsPublished(outbox.getId());
                log.info("{} 이벤트 발행 성공: orderId={}", outbox.getEventType(), outbox.getOrderId());
            } catch (Exception e) {
                couponOutboxEventRepository.markAsFailed(outbox.getId());
                log.error("{} 이벤트 발행 실패: orderId={}, error={}", outbox.getEventType(), outbox.getOrderId(), e.getMessage());
            }
        }
    }

    private void publishEvent(CouponOutboxEvent outbox) throws Exception {
        String eventType = outbox.getEventType();
        String payload = outbox.getPayload();

        switch (eventType) {
            case "CouponUsedEvent" -> {
                CouponUsedEvent event = objectMapper.readValue(payload, CouponUsedEvent.class);
                eventProducer.publishCouponUsedEvent(event);
            }
            case "CouponUsedFailedEvent" -> {
                CouponUsedFailedEvent event = objectMapper.readValue(payload, CouponUsedFailedEvent.class);
                eventProducer.publishCouponUseFailedEvent(event);
            }
            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        }
    }
}

