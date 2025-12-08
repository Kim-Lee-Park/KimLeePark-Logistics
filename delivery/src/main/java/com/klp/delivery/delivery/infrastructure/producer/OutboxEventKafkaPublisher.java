package com.klp.delivery.delivery.infrastructure.producer;

import static com.klp.delivery.global.config.KafkaTopicConfig.DELIVERY_EVENTS;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.delivery.delivery.domain.entity.outbox.DeliveryOutboxEvent;
import com.klp.delivery.delivery.domain.event.OrderDeliveryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventKafkaPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;


    public void publishToKafka(DeliveryOutboxEvent event) throws Exception {
        Object eventData = deserializePayload(event.getPayload(), event.getEventType());

        kafkaTemplate.send(DELIVERY_EVENTS,
            event.getDeliveryId().toString(), eventData)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Kafka 발행 실패: eventId={}", event.getId(), ex);
                    throw new RuntimeException("Kafka 발행 실패", ex);
                } else {
                    log.info("Kafka 발행 성공: eventId={}, partition={}, offset={}",
                        event.getId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                }
            }).get();
    }


    private Object deserializePayload(String payload, String eventType) throws Exception {
        return switch (eventType) {
            case "DELIVERY_CREATED", "DELIVERY_SHIPPING", "DELIVERY_COMPLETED" ->
                objectMapper.readValue(payload, OrderDeliveryEvent.class);
            default -> throw new IllegalArgumentException("정의하지 않은 이벤트 타입: " + eventType);
        };
    }
}

