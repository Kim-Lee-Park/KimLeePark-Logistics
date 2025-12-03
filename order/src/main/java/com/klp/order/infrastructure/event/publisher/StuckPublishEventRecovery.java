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
    private static final long STUCK_THRESHOLD_MINUTES = 5;

    @Scheduled(fixedDelay = 60000) // 1분마다
    public void recoverStuckPublishingEvents() {
        try {
            List<OrderOutboxEvent> stuckEvents =
                orderOutboxEventRepository.findStuckPublishingEvents();

            if (stuckEvents.isEmpty()) {
                return;
            }

            LocalDateTime threshold = LocalDateTime.now()
                .minusMinutes(STUCK_THRESHOLD_MINUTES);

            for (OrderOutboxEvent event : stuckEvents) {
                // lastRetryAt이 임계값보다 오래된 경우만 복구
                if (event.getLastRetryAt() != null
                    && event.getLastRetryAt().isBefore(threshold)) {

                    event.resetToPending();
                    orderOutboxEventRepository.save(event);

                }
            }
        } catch (Exception e) {
            log.error("PUBLISHING 상태 이벤트 복구 중 오류 발생", e);
        }
    }

    // 실패 이벤트 관리
    @Scheduled(cron = "0 */10 * * * *")
    public void monitorFailedEvents() {
        try {
            List<OrderOutboxEvent> failedEvents =
                orderOutboxEventRepository.findFailedEvents();
            if (failedEvents.isEmpty()) {
                log.info("현재 실패한 이벤트는 없습니다.");
                return;
            }

            log.error("========================================");
            log.error("⚠️ FAILED 상태 이벤트 {}건 발견!", failedEvents.size());
            log.error("⚠️ 수동 처리가 필요합니다!");
            log.error("========================================");

            for (OrderOutboxEvent event : failedEvents) {
                log.error("FAILED 이벤트 상세: " +
                        "eventId={}, " +
                        "eventType={}, " +
                        "aggregateId={}, " +
                        "retryCount={}, " +
                        "errorMessage={}",
                    event.getId(),
                    event.getEventType(),
                    event.getAggregateId(),
                    event.getRetryCount(),
                    event.getPayload());
            }

        } catch (Exception e) {
            log.error("FAILED 이벤트 모니터링 중 오류 발생", e);
        }
    }
}
