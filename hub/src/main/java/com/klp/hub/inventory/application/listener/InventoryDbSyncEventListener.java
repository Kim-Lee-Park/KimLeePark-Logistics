package com.klp.hub.inventory.application.listener;

import com.klp.hub.inventory.domain.InventoryReservation;
import com.klp.hub.inventory.domain.event.InventoryDbSyncEvent;
import com.klp.hub.inventory.domain.repository.InventoryReservationRepository;
import com.klp.hub.inventory.infrastructure.kafka.config.KafkaTopicConfig;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryDbSyncEventListener {

    private final InventoryReservationRepository reservationRepository;

    @KafkaListener(
        topics = KafkaTopicConfig.INVENTORY_DB_SYNC_TOPIC,
        groupId = "inventory-db-sync-group",
        containerFactory = "inventoryKafkaListenerContainerFactory"
    )
    @Transactional
    public void handleDbSyncEvent(
        @Payload List<InventoryDbSyncEvent> events,
        Acknowledgment acknowledgment
    ) {
        log.info("DB 동기화 이벤트 배치 수신: eventCount={}", events.size());

        try {
            List<InventoryReservation> reservations = events.stream()
                .flatMap(event -> event.items().stream()
                    .map(item -> InventoryReservation.create(
                        event.orderId(),
                        item.productId(),
                        item.hubId(),
                        item.quantity(),
                        event.idempotencyKey(),
                        LocalDateTime.now().plusMinutes(15)
                    ))
                )
                .toList();

            reservationRepository.saveAllInBatch(reservations);

            log.info("선점 레코드 bulk insert 완료: reservationCount={}", reservations.size());

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
        } catch (Exception e) {
            log.error("DB 동기화 이벤트 배치 처리 실패: eventCount={}", events.size(), e);
            throw e;
        }
    }
}
