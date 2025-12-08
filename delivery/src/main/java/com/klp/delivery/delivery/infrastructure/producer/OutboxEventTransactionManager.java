package com.klp.delivery.delivery.infrastructure.producer;

import com.klp.delivery.delivery.domain.entity.outbox.DeliveryOutboxEvent;
import com.klp.delivery.delivery.domain.entity.outbox.DeliveryOutboxStatus;
import com.klp.delivery.delivery.domain.repository.DeliveryOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxEventTransactionManager {

    private final DeliveryOutboxEventRepository deliveryOutboxEventRepository;
    private final OutboxEventKafkaPublisher kafkaPublisher;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishEvent(DeliveryOutboxEvent event) {
        try {
            // 1단계: PUBLISHING으로 상태 변경 및 즉시 커밋
            event.markAsPublishing();
            deliveryOutboxEventRepository.saveAndFlush(event);
            log.info("이벤트 상태 PUBLISHING으로 변경: eventId={}", event.getId());

            // 2단계: Kafka 발행
            kafkaPublisher.publishToKafka(event);
            log.info("Kafka 발행 완료: eventId={}", event.getId());

            // 3단계: PUBLISHED로 상태 변경 및 즉시 커밋
            event.markAsPublished();
            deliveryOutboxEventRepository.saveAndFlush(event);
            log.info("이벤트 상태 PUBLISHED로 변경: eventId={}", event.getId());

        } catch (Exception e) {
            log.error("이벤트 발행 실패: eventId={}", event.getId(), e);
            handlePublishFailure(event, e);
        }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePublishFailure(DeliveryOutboxEvent event, Exception e) {
        event.markAsFailed();
        deliveryOutboxEventRepository.saveAndFlush(event);

        if (event.getStatus() == DeliveryOutboxStatus.FAILED) {
            log.error("이벤트 최종 실패 ({}회 초과): eventId={}, eventType={}",
                event.getRetryCount(), event.getId(), event.getEventType(), e);
        } else {
            long nextRetrySeconds = event.getBackoffMillis() / 1000;
            log.warn("이벤트 발행 실패 (재시도 {}/{} 회): eventId={}, nextRetry={}초 후",
                event.getRetryCount(), 20, event.getId(), nextRetrySeconds, e);
        }
    }
}

