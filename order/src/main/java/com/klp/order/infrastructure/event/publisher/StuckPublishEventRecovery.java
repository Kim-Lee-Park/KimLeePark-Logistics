package com.klp.order.infrastructure.event.publisher;

import com.klp.order.domain.entity.outbox.OrderOutboxEvent;
import com.klp.order.domain.repository.OrderOutboxEventRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StuckPublishEventRecovery {

    private final OrderOutboxEventRepository orderOutboxEventRepository;

    // AWS/Google 권장: 3분 이상 PUBLISHING 상태면 stuck으로 간주
    private static final long STUCK_THRESHOLD_MINUTES = 3;

    /**
     * PUBLISHING 상태로 멈춘 이벤트 복구 30초마다 실행 (AWS/Google 권장)
     */
    @Scheduled(fixedDelay = 30000)
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
                // 3분 이상 PUBLISHING 상태인 경우만 복구
                if (event.getLastRetryAt() != null
                    && event.getLastRetryAt().isBefore(threshold)) {

                    event.resetToPending();
                    orderOutboxEventRepository.save(event);
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
     * FAILED 이벤트 모니터링 및 알림 5분마다 실행
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
            log.error("⚠️ 최대 재시도 횟수(15회) 초과로 실패 처리됨");
            log.error("⚠️ 수동 처리가 필요합니다!");
            log.error("========================================");

            for (OrderOutboxEvent event : failedEvents) {
                log.error("FAILED 이벤트 상세: " +
                        "eventId={}, " +
                        "eventType={}, " +
                        "aggregateId={}, " +
                        "retryCount={}, " +
                        "lastRetryAt={}",
                    event.getId(),
                    event.getEventType(),
                    event.getAggregateId(),
                    event.getRetryCount(),
                    event.getLastRetryAt());
            }

        } catch (Exception e) {
            log.error("FAILED 이벤트 모니터링 중 오류 발생", e);
        }
    }
}