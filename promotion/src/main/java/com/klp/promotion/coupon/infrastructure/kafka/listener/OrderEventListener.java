package com.klp.promotion.coupon.infrastructure.kafka.listener;

import com.klp.promotion.coupon.application.facade.UserCouponFacade;
import com.klp.promotion.coupon.domain.event.OrderCancelledEvent;
import com.klp.promotion.coupon.domain.event.OrderCreatedEvent;
import com.klp.promotion.coupon.domain.event.OrderFailedEvent;
import com.klp.promotion.coupon.domain.event.WhichRollback;
import com.klp.promotion.coupon.infrastructure.kafka.config.KafkaTopicConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Component
@RequiredArgsConstructor
@KafkaListener(
    topics = KafkaTopicConfig.ORDER_TOPIC,
    groupId = "promotion-service-group",
    containerFactory = "couponKafkaListenerContainerFactory"
)
public class OrderEventListener {

    private final UserCouponFacade userCouponFacade;

    @KafkaHandler
    @Transactional
    public void handleOrderFailed(@Payload OrderFailedEvent event) {
        log.info("=== 주문 실패 이벤트 수신: orderId={}, userCouponId={}, rollbackType={} ===",
            event.orderId(), event.userCouponId(), event.type());

        try {
            WhichRollback rollbackType = event.type();

            if (rollbackType == WhichRollback.PROMOTION || rollbackType == WhichRollback.ALL) {
                log.info("쿠폰 복구 시작 - userCouponId: {}", event.userCouponId());
                userCouponFacade.couponRestored(event.userCouponId());
                log.info("쿠폰 복구 완료 - userCouponId: {}", event.userCouponId());
            } else {
                log.info("쿠폰 복구 불필요 - orderId: {}, rollbackType: {}",
                    event.orderId(), rollbackType);
            }

        } catch (Exception e) {
            log.error("주문 실패 이벤트 처리 실패: orderId={}, userCouponId={}, error={}",
                event.orderId(), event.userCouponId(), e.getMessage(), e);
            throw e;
        }
    }

    @KafkaHandler
    @Transactional
    public void handleOrderCreated(@Payload OrderCreatedEvent event) {
        log.info("주문 생성 쿠폰 사용 준비 - orderId: {}", event.orderId());
    }

    @KafkaHandler
    @Transactional
    public void handleOrderCancelled(@Payload OrderCancelledEvent event) {
        log.info("주문 취소 쿠폰 복구 준비 - orderId: {}", event.orderId());
    }

    @KafkaHandler(isDefault = true)
    public void handleUnknown(Object event) {
        log.debug("Promotion에서 처리하지 않는 Order 이벤트 (무시): {}",
            event.getClass().getSimpleName());
    }
}