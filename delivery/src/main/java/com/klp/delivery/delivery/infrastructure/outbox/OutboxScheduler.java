package com.klp.delivery.delivery.infrastructure.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.delivery.delivery.domain.entity.outbox.DeliveryOutboxEvent;
import com.klp.delivery.delivery.domain.event.DeliveryArrivedEvent;
import com.klp.delivery.delivery.domain.event.DeliveryCreatedEvent;
import com.klp.delivery.delivery.domain.event.DeliveryNotificationEvent;
import com.klp.delivery.delivery.domain.event.DeliveryShippingEvent;
import com.klp.delivery.delivery.domain.repository.DeliveryOutboxEventRepository;
import com.klp.delivery.delivery.infrastructure.producer.DeliveryEventProducer;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxScheduler {

    private final DeliveryOutboxEventRepository deliveryOutboxEventRepository;
    private final DeliveryEventProducer eventProducer;
    private final ObjectMapper objectMapper;

    private static final int BATCH_SIZE = 100;

    @Scheduled(fixedDelay = 1000)
    public void publishPendingEvents() {
        List<DeliveryOutboxEvent> pendingEvents = deliveryOutboxEventRepository.findPendingEvents(
            BATCH_SIZE);

        for (DeliveryOutboxEvent outbox : pendingEvents) {
            try {
                publishEvent(outbox);
                deliveryOutboxEventRepository.markAsPublished(outbox.getId());
                log.info("{} 이벤트 발행 성공: deliveryId={}", outbox.getEventType(),
                    outbox.getDeliveryId());
            } catch (Exception e) {
                deliveryOutboxEventRepository.markAsFailed(outbox.getId());
                log.error("{} 이벤트 발행 실패: deliveryId={}, error={}", outbox.getEventType(),
                    outbox.getDeliveryId(), e.getMessage());
            }
        }
    }

    private void publishEvent(DeliveryOutboxEvent outbox) throws Exception {
        String eventType = outbox.getEventType();
        String payload = outbox.getPayload();

        switch (eventType) {
            case "DELIVERY_CREATED" -> {
                // 배송 생성 이벤트 발행
                DeliveryCreatedEvent createEvent = objectMapper.readValue(payload,
                    DeliveryCreatedEvent.class);
                eventProducer.publishCreatedEvent(createEvent);
            }
            case "DELIVERY_NOTIFICATION" -> {
                DeliveryNotificationEvent notificationEvent = objectMapper.readValue(payload,
                    DeliveryNotificationEvent.class);
                eventProducer.publishNotificationEvent(notificationEvent);
            }
            case "DELIVERY_SHIPPING" -> {
                DeliveryShippingEvent shippingEvent = objectMapper.readValue(payload,
                    DeliveryShippingEvent.class);
                eventProducer.publishShippingEvent(shippingEvent);
            }
            case "DELIVERY_COMPLETED" -> {
                DeliveryArrivedEvent arrivedEvent = objectMapper.readValue(payload,
                    DeliveryArrivedEvent.class);
                eventProducer.publishArrivedEvent(arrivedEvent);
            }
            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        }
    }
}

