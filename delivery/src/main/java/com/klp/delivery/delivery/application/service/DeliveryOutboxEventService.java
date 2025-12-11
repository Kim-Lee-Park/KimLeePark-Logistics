package com.klp.delivery.delivery.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.delivery.delivery.domain.entity.outbox.DeliveryOutboxEvent;
import com.klp.delivery.delivery.domain.event.DeliveryCreatedEvent;
import com.klp.delivery.delivery.domain.event.DeliveryNotificationEvent;
import com.klp.delivery.delivery.domain.event.DeliveryShippingEvent;
import com.klp.delivery.delivery.domain.event.DeliveryArrivedEvent;
import com.klp.delivery.delivery.domain.repository.DeliveryOutboxEventRepository;
import com.klp.delivery.delivery.exception.DeliveryErrorCode;
import com.klp.delivery.global.exception.BusinessException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryOutboxEventService {

    private final DeliveryOutboxEventRepository deliveryOutboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void saveCreatedEvent(UUID deliveryId, UUID orderId, DeliveryCreatedEvent event) {
        saveOutboxEvent(event, deliveryId, orderId, "DELIVERY_CREATED");
    }

    @Transactional
    public void saveNotificationEvent(UUID deliveryId, UUID orderId, DeliveryNotificationEvent event) {
        saveOutboxEvent(event, deliveryId, orderId, "DELIVERY_NOTIFICATION");
    }

    @Transactional
    public void saveShippingEvent(UUID deliveryId, UUID orderId, DeliveryShippingEvent event) {
        saveOutboxEvent(event, deliveryId, orderId, "DELIVERY_SHIPPING");
    }

    @Transactional
    public void saveArrivedEvent(UUID deliveryId, UUID orderId, DeliveryArrivedEvent event) {
        saveOutboxEvent(event, deliveryId, orderId, "DELIVERY_COMPLETED");
    }

    // 이벤트 db 저장
    private void saveOutboxEvent(Object eventData, UUID deliveryId, UUID orderId, String eventType) {
        try {
            // JSON 직렬화
            String payload = objectMapper.writeValueAsString(eventData);

            // DeliveryOutboxEvent 생성
            DeliveryOutboxEvent outboxEvent = DeliveryOutboxEvent.create(
                deliveryId,
                orderId,
                eventType,
                payload
            );

            // DB 저장
            deliveryOutboxEventRepository.save(outboxEvent);

            log.info("Outbox 이벤트 저장 성공: eventType={}, deliveryId={}, orderId={}", 
                eventType, deliveryId, orderId);

        } catch (JsonProcessingException e) {
            log.error("이벤트 JSON 변환 실패: eventType={}, deliveryId={}, orderId={}", 
                eventType, deliveryId, orderId, e);
            throw new BusinessException(DeliveryErrorCode.EVENT_SERIALIZATION_FAILED);

        } catch (DataAccessException e) {
            log.error("Outbox 이벤트 저장 실패 - DB 장애 감지: eventType={}, deliveryId={}, orderId={}, error={}", 
                eventType, deliveryId, orderId, e.getClass().getSimpleName(), e);
            throw e; // 재발생시켜 상위 트랜잭션 롤백

        } catch (Exception e) {
            log.error("Outbox 이벤트 저장 실패 - 알 수 없는 오류: eventType={}, deliveryId={}, orderId={}", 
                eventType, deliveryId, orderId, e);
            throw new DataAccessException("Outbox 저장 중 오류 발생", e) {
            };
        }
    }
}

