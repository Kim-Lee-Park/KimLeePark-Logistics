package com.klp.order.order.infrastructure.event.publisher;

import com.klp.order.order.domain.entity.outbox.OrderOutboxEvent;
import com.klp.order.order.domain.repository.OrderOutboxEventRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderOutboxEventPublisher {

    private final OrderOutboxEventRepository orderOutboxEventRepository;
    private final OutboxEventTransactionManager transactionManager;

    private static final long STUCK_THRESHOLD_MINUTES = 3;


    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {
        List<OrderOutboxEvent> pendingEvents =
            orderOutboxEventRepository.findPendingEvents();

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("발행 대기 중인 이벤트 {}건 처리 시작", pendingEvents.size());

        for (OrderOutboxEvent event : pendingEvents) {
            if (!event.shouldRetryNow()) {
                continue;
            }
            transactionManager.publishEvent(event);
        }

        log.info("발행 대기 이벤트 처리 완료");
    }

    /**
     * PUBLISHING 상태 복구 - 30초마다 실행 오래 멈춘 PUBLISHING 이벤트를 PENDING으로 복구
     */
    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void recoverStuckPublishingEvents() {
        try {
            List<OrderOutboxEvent> stuckEvents =
                orderOutboxEventRepository.findStuckPublishingEvents();

            if (stuckEvents.isEmpty()) {
                return;
            }

            LocalDateTime threshold = LocalDateTime.now()
                .minusMinutes(STUCK_THRESHOLD_MINUTES);

            int recoveredCount = 0;
            for (OrderOutboxEvent event : stuckEvents) {
                if (event.getLastRetryAt() != null
                    && event.getLastRetryAt().isBefore(threshold)) {

                    event.resetToPending();
                    orderOutboxEventRepository.saveAndFlush(event);
                    recoveredCount++;

                    log.warn("PUBLISHING 상태 이벤트 복구: eventId={}, retryCount={}",
                        event.getId(), event.getRetryCount());
                }
            }

            if (recoveredCount > 0) {
                log.info("총 {}건의 PUBLISHING 이벤트 복구 완료", recoveredCount);
            }

        } catch (Exception e) {
            log.error("PUBLISHING 상태 이벤트 복구 중 오류 발생", e);
        }
    }

    /**
     * FAILED 이벤트 모니터링 - 5분마다 실행 최종 실패한 이벤트 알림 및 로깅
     */
    @Scheduled(cron = "0 */5 * * * *")
    public void monitorFailedEvents() {
        try {
            List<OrderOutboxEvent> failedEvents =
                orderOutboxEventRepository.findFailedEvents();

            if (failedEvents.isEmpty()) {
                return;
            }

            log.error("========================================");
            log.error("⚠️ FAILED 상태 이벤트 {}건 발견!", failedEvents.size());
            log.error("⚠️ 최대 재시도 횟수(20회) 초과로 실패 처리됨");
            log.error("⚠️ 수동 처리가 필요합니다!");
            log.error("========================================");

            for (OrderOutboxEvent event : failedEvents) {
                log.error("FAILED 이벤트 상세: " +
                        "eventId={}, " +
                        "eventType={}, " +
                        "orderId={}, " +
                        "retryCount={}, " +
                        "lastRetryAt={}",
                    event.getId(),
                    event.getEventType(),
                    event.getOrderId(),
                    event.getRetryCount(),
                    event.getLastRetryAt());
            }

        } catch (Exception e) {
            log.error("FAILED 이벤트 모니터링 중 오류 발생", e);
        }
    }
}

