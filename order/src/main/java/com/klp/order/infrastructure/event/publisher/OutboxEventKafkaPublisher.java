package com.klp.order.infrastructure.event.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.order.domain.entity.outbox.OrderOutboxEvent;
import com.klp.order.infrastructure.event.event.OrderCancelledEvent;
import com.klp.order.infrastructure.event.event.OrderCreatedEvent;
import com.klp.order.infrastructure.event.event.OrderPaidEvent;
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


    public void publishToKafka(OrderOutboxEvent event) throws Exception {
        String topic = determineTopicByEventType(event.getEventType());
        Object eventData = deserializePayload(event.getPayload(), event.getEventType());

        kafkaTemplate.send(topic, event.getAggregateId().toString(), eventData)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Kafka 발행 실패: topic={}, eventId={}", topic, event.getId(), ex);
                    throw new RuntimeException("Kafka 발행 실패", ex);
                } else {
                    log.info("Kafka 발행 성공: topic={}, eventId={}, partition={}, offset={}",
                        topic, event.getId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                }
            }).get();
    }

    private String determineTopicByEventType(String eventType) {
        return switch (eventType) {
            case "ORDER_CREATED" -> "order.created";
            case "ORDER_CANCELLED" -> "order.cancelled";
            case "ORDER_PAID" -> "order.paid";
            default -> throw new IllegalArgumentException("정의하지 않은 토픽 타입: " + eventType);
        };
    }

    private Object deserializePayload(String payload, String eventType) throws Exception {
        return switch (eventType) {
            case "ORDER_CREATED" -> objectMapper.readValue(payload, OrderCreatedEvent.class);
            case "ORDER_CANCELLED" -> objectMapper.readValue(payload, OrderCancelledEvent.class);
            case "ORDER_PAID" -> objectMapper.readValue(payload, OrderPaidEvent.class);
            default -> throw new IllegalArgumentException("정의하지 않은 이벤트 타입: " + eventType);
        };
    }
}
