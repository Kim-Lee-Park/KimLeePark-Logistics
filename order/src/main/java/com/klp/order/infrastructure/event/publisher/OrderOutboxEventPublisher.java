package com.klp.order.infrastructure.event.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.order.domain.entity.outbox.OrderOutboxEvent;
import com.klp.order.domain.entity.outbox.OrderOutboxStatus;
import com.klp.order.domain.repository.OrderOutboxEventRepository;
import com.klp.order.infrastructure.event.event.OrderCancelledEvent;
import com.klp.order.infrastructure.event.event.OrderCreatedEvent;
import com.klp.order.infrastructure.event.event.OrderPaidEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderOutboxEventPublisher {

    private final OrderOutboxEventRepository orderOutboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 5000) //5초마다 실행
    public void publishPendingEvents() {
        List<OrderOutboxEvent> pendingEvents = orderOutboxEventRepository.findPendingEvents();

        if (pendingEvents.isEmpty()) {
            return;
        }
        log.info("발행 대기 중인 이벤트 {}건 처리 시작", pendingEvents.size());

        for (OrderOutboxEvent event : pendingEvents) {
            if (!event.shouldRetryNow()) {
                continue;
            }
            publishEventInSeperateTransaction(event);
        }
    }

    // 분리 이유 : Outbox에 이벤트가 저장되고 트랜잭션이 커밋된 후 Publish가 실행되기 전에 애플리케이션이 다운 되면 이벤트가 발행되지 않기 때문에 분리
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishEventInSeperateTransaction(OrderOutboxEvent event) {
        try {
            event.markAsPublishing();
            orderOutboxEventRepository.save(event);
            publishEvent(event);
            event.markAsPublished();
            orderOutboxEventRepository.save(event);
        } catch (Exception e) {
            // 실패 시 재시도 카운트만 증가 (PENDING 유지)
            // 그 대신 이제 재시도 카운트가 20 이상이면 markAsFailed에서 자동으로 Failed로 상태변화
            event.markAsFailed();
            orderOutboxEventRepository.save(event);
            if (event.getStatus() == OrderOutboxStatus.FAILED) {
                log.error("이벤트 최종 실패 (20회 초과): eventId={}, eventType={}",
                    event.getId(), event.getEventType(), e);
            } else {
                // 재시도 예정
                long nextRetrySeconds = event.getBackoffMillis() / 1000;
                log.warn("이벤트 발행 실패 (재시도 {}/20회): eventId={}, nextRetry={}초 후",
                    event.getRetryCount(), event.getId(), nextRetrySeconds, e);
            }
        }
    }

    private void publishEvent(OrderOutboxEvent event) throws Exception {
        String topic = determineTopicByEventType(event.getEventType());
        Object eventData = deserializePayload(event.getPayload(), event.getEventType());

        kafkaTemplate.send(topic, event.getAggregateId().toString(), eventData);
        log.info("Kafka 발행 완료: topic={}, eventId={}", topic, event.getId());
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
