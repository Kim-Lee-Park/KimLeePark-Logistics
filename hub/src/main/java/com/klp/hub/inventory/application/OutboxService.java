package com.klp.hub.inventory.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.hub.global.exception.BusinessException;
import com.klp.hub.inventory.domain.event.InventoryDeductedEvent;
import com.klp.hub.inventory.domain.event.InventoryDeductedFailedEvent;
import com.klp.hub.inventory.domain.event.InventoryReplenishedEvent;
import com.klp.hub.inventory.domain.outbox.InventoryOutbox;
import com.klp.hub.inventory.domain.outbox.InventoryOutboxRepository;
import com.klp.hub.inventory.exception.InventoryErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {

    private final InventoryOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    private static final String DEDUCT_EVENT_TYPE = "InventoryDeductedEvent";
    private static final String DEDUCT_FAILED_EVENT_TYPE = "InventoryDeductedFailedEvent";
    private static final String REPLENISH_EVENT_TYPE = "InventoryReplenishedEvent";

    @Transactional
    public void saveInventoryDeductedEvent(InventoryDeductedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            InventoryOutbox outbox = InventoryOutbox.create(
                event.orderId(),
                DEDUCT_EVENT_TYPE,
                payload
            );
            outboxRepository.save(outbox);
            log.info("Outbox 저장 완료: orderId={}, eventType={}", event.orderId(), DEDUCT_EVENT_TYPE);
        } catch (JsonProcessingException e) {
            log.error("Outbox 직렬화 실패: orderId={}", event.orderId(), e);
            throw new BusinessException(InventoryErrorCode.OUTBOX_SERIALIZATION_FAILED);
        }
    }

    @Transactional
    public void saveInventoryReplenishedEvent(InventoryReplenishedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            InventoryOutbox outbox = InventoryOutbox.create(
                event.orderId(),
                REPLENISH_EVENT_TYPE,
                payload
            );
            outboxRepository.save(outbox);
            log.info("Outbox 저장 완료: orderId={}, eventType={}", event.orderId(),
                REPLENISH_EVENT_TYPE);
        } catch (JsonProcessingException e) {
            log.error("Outbox 직렬화 실패: orderId={}", event.orderId(), e);
            throw new BusinessException(InventoryErrorCode.OUTBOX_SERIALIZATION_FAILED);
        }
    }

    @Transactional
    public void saveInventoryDedictedFailedEvent(InventoryDeductedFailedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            InventoryOutbox outbox = InventoryOutbox.create(
                event.orderId(),
                DEDUCT_FAILED_EVENT_TYPE,
                payload
            );
            outboxRepository.save(outbox);
            log.info("Outbox 저장 완료: orderId={}, eventType={}", event.orderId(),
                DEDUCT_FAILED_EVENT_TYPE);
        } catch (JsonProcessingException e) {
            log.error("Outbox 직렬화 실패: orderId={}", event.orderId(), e);
            throw new BusinessException(InventoryErrorCode.OUTBOX_SERIALIZATION_FAILED);
        }
    }
}
