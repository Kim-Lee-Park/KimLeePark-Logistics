package com.klp.order.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.global.exception.BusinessException;
import com.klp.global.exception.OrderErrorCode;
import com.klp.order.domain.entity.outbox.OrderOutboxEvent;
import com.klp.order.domain.repository.OrderOutboxEventRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderOutboxEventService {

    private final OrderOutboxEventRepository orderOutboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void saveEvent(UUID orderId,
        String eventType, Object eventData) {

        try {
            // 1. JSON 직렬화
            String payload = serializeEventData(eventData, eventType);

            OrderOutboxEvent outboxEvent = OrderOutboxEvent.create(
                orderId,
                eventType,
                payload
            );

            saveOutboxEvent(outboxEvent, orderId, eventType);

            log.info("Outbox 이벤트 저장 성공: eventType={}, orderId={}",
                eventType, orderId);

        } catch (JsonProcessingException e) {
            // JSON 직렬화 실패
            log.error("이벤트 직렬화 실패: eventType={}, orderId={}",
                eventType, orderId, e);
            throw new BusinessException(
                OrderErrorCode.ORDER_CREATION_FAILED,
                "이벤트 데이터 직렬화 실패: " + e.getMessage()
            );

        } catch (DataAccessException e) {
            // DB 접근 오류 (커넥션 장애, 제약조건 위반 등)
            log.error("Outbox 이벤트 DB 저장 실패 (DB 장애): eventType={}, orderId={}",
                eventType, orderId, e);
            throw new BusinessException(
                OrderErrorCode.ORDER_CREATION_FAILED,
                "데이터베이스 장애로 이벤트 저장 실패: " + e.getMessage()
            );

        } catch (Exception e) {
            // 기타 예상치 못한 오류
            log.error("Outbox 이벤트 저장 중 예상치 못한 오류: eventType={}, orderId={}",
                eventType, orderId, e);
            throw new BusinessException(
                OrderErrorCode.ORDER_CREATION_FAILED,
                "이벤트 저장 중 알 수 없는 오류 발생: " + e.getMessage()
            );
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFailedEvent(UUID orderId, String eventType, Object eventData) {
        try {
            String payload = serializeEventData(eventData, eventType);
            OrderOutboxEvent outboxEvent = OrderOutboxEvent.create(orderId, eventType, payload);
            saveOutboxEvent(outboxEvent, orderId, eventType);
            log.info("실패 이벤트 저장 성공: eventType={}, orderId={}", eventType, orderId);
        } catch (Exception e) {
            log.error("실패 이벤트 저장 실패: eventType={}, orderId={}", eventType, orderId, e);
        }
    }

    // JSON 직렬화
    private String serializeEventData(Object eventData, String eventType)
        throws JsonProcessingException {

        try {
            return objectMapper.writeValueAsString(eventData);
        } catch (JsonProcessingException e) {
            log.error("이벤트 JSON 변환 실패: eventType={}", eventType, e);
            throw e;
        }
    }

    private void saveOutboxEvent(OrderOutboxEvent outboxEvent,
        UUID orderId, String eventType) {

        try {
            orderOutboxEventRepository.save(outboxEvent);

        } catch (DataAccessException e) {
            log.error("Outbox 이벤트 저장 실패 - DB 장애 감지: eventType={}, orderId={}, error={}",
                eventType, orderId, e.getClass().getSimpleName(), e);
            throw e; // 재발생시켜 상위 트랜잭션 롤백

        } catch (Exception e) {
            log.error("Outbox 이벤트 저장 실패 - 알 수 없는 오류: eventType={}, orderId={}",
                eventType, orderId, e);
            throw new DataAccessException("Outbox 저장 중 오류 발생", e) {
            };
        }
    }
}