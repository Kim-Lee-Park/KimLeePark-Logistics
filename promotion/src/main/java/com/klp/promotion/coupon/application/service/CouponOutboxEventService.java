package com.klp.promotion.coupon.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.promotion.coupon.domain.entity.outbox.CouponOutboxEvent;
import com.klp.promotion.coupon.domain.event.CouponRestoredEvent;
import com.klp.promotion.coupon.domain.event.CouponUsedEvent;
import com.klp.promotion.coupon.domain.event.CouponUsedFailedEvent;
import com.klp.promotion.coupon.domain.repository.CouponOutboxEventRepository;
import com.klp.promotion.coupon.common.exception.CouponErrorCode;
import com.klp.promotion.global.exception.BusinessException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponOutboxEventService {

    private final CouponOutboxEventRepository couponOutboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void saveEvent(UUID orderId, CouponUsedEvent event) {
        saveOutboxEvent(event, orderId, "CouponUsedEvent");
    }

    @Transactional
    public void failEvent(UUID orderId, CouponUsedFailedEvent event) {
        saveOutboxEvent(event, orderId, "CouponUsedFailedEvent");
    }

    @Transactional
    public void cancelEvent(UUID orderId, CouponRestoredEvent event) {
        saveOutboxEvent(event, orderId, "CouponRestoredEvent");
    }

    // 이벤트  db 저장
    private void saveOutboxEvent(Object eventData, UUID orderId, String eventType) {

        try {
            // JSON 직렬화
            String payload = objectMapper.writeValueAsString(eventData);

            // CouponOutboxEvent 생성
            CouponOutboxEvent outboxEvent = CouponOutboxEvent.create(orderId, eventType, payload);

            // DB 저장
            couponOutboxEventRepository.save(outboxEvent);

            log.info("Outbox 이벤트 저장 성공: eventType={}, orderId={}", eventType, orderId);

        } catch (JsonProcessingException e) {

            log.error("이벤트 JSON 변환 실패: eventType={}, orderId={}", eventType, orderId, e);
            throw new BusinessException(CouponErrorCode.EVENT_SERIALIZATION_FAILED);

        } catch (DataAccessException e) {
            log.error("Outbox 이벤트 저장 실패 - DB 장애 감지: eventType={}, orderId={}, error={}", eventType,
                orderId, e.getClass().getSimpleName(), e);
            throw e; // 재발생시켜 상위 트랜잭션 롤백

        } catch (Exception e) {
            log.error("Outbox 이벤트 저장 실패 - 알 수 없는 오류: eventType={}, orderId={}", eventType, orderId,
                e);
            throw new DataAccessException("Outbox 저장 중 오류 발생", e) {
            };
        }
    }
}

