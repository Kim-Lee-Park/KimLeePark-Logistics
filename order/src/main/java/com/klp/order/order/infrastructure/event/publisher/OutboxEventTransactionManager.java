package com.klp.order.order.infrastructure.event.publisher;

import com.klp.order.order.domain.entity.outbox.OrderOutboxEvent;
import com.klp.order.order.domain.entity.outbox.OrderOutboxStatus;
import com.klp.order.order.domain.repository.OrderOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxEventTransactionManager {

    private final OrderOutboxEventRepository orderOutboxEventRepository;
    private final OutboxEventKafkaPublisher kafkaPublisher;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishEvent(OrderOutboxEvent event) {
        try {
            // 1단계: PUBLISHING으로 상태 변경 및 즉시 커밋
            event.markAsPublishing();
            orderOutboxEventRepository.saveAndFlush(event);
            log.info("이벤트 상태 PUBLISHING으로 변경: eventId={}", event.getId());

            // 2단계: Kafka 발행
            kafkaPublisher.publishToKafka(event);
            log.info("Kafka 발행 완료: eventId={}", event.getId());

            // 3단계: PUBLISHED로 상태 변경 및 즉시 커밋
            event.markAsPublished();
            orderOutboxEventRepository.saveAndFlush(event);
            log.info("이벤트 상태 PUBLISHED로 변경: eventId={}", event.getId());

        } catch (Exception e) {
            log.error("이벤트 발행 실패: eventId={}", event.getId(), e);
            handlePublishFailure(event, e);
        }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePublishFailure(OrderOutboxEvent event, Exception e) {
        event.markAsFailed();
        orderOutboxEventRepository.saveAndFlush(event);

        if (event.getStatus() == OrderOutboxStatus.FAILED) {
            log.error("이벤트 최종 실패 ({}회 초과): eventId={}, eventType={}",
                event.getRetryCount(), event.getId(), event.getEventType(), e);
        } else {
            long nextRetrySeconds = event.getBackoffMillis() / 1000;
            log.warn("이벤트 발행 실패 (재시도 {}/{} 회): eventId={}, nextRetry={}초 후",
                event.getRetryCount(), 20, event.getId(), nextRetrySeconds, e);
        }
    }
}