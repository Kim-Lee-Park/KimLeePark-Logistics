package com.klp.promotion.coupon.infrastructure.kafka.listener;

import com.klp.promotion.coupon.application.facade.UserCouponFacade;
import com.klp.promotion.coupon.domain.event.OrderFailedEvent;
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
    groupId = "order-service-group",
    containerFactory = "couponKafkaListenerContainerFactory"
)
public class OrderEventListener {

    private final UserCouponFacade userCouponFacade;

    @KafkaHandler
    @Transactional
    public void handleIOrderFailed(@Payload OrderFailedEvent event) {
        log.info("주문 실패 이벤트 수신: orderId={}, userCouponId={}", event.orderId(), event.userCouponId());

        userCouponFacade.couponRestored(event.userCouponId());
    }


}
