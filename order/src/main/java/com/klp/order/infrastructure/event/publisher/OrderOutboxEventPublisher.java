package com.klp.order.infrastructure.event.publisher;

import com.klp.order.domain.entity.outbox.OrderOutboxEvent;
import com.klp.order.domain.repository.OrderOutboxEventRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderOutboxEventPublisher {

    private final OrderOutboxEventRepository orderOutboxEventRepository;
    private final OutboxEventTransactionManager transactionManager;


    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {
        List<OrderOutboxEvent> pendingEvents = orderOutboxEventRepository.findPendingEvents();

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("발행 대기 중인 이벤트 {}건 처리 시작", pendingEvents.size());

        for (OrderOutboxEvent event : pendingEvents) {
            // 백오프 시간 체크
            if (!event.shouldRetryNow()) {
                continue;
            }
            transactionManager.publishEvent(event);
        }

        log.info("발행 대기 이벤트 처리 완료");
    }
}

