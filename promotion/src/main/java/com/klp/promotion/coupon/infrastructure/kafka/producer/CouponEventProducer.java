package com.klp.promotion.coupon.infrastructure.kafka.producer;

import com.klp.promotion.coupon.domain.event.CouponCancelledEvent;
import com.klp.promotion.coupon.domain.event.CouponUsedFailedEvent;
import com.klp.promotion.coupon.domain.event.CouponUsedEvent;
import com.klp.promotion.coupon.infrastructure.kafka.config.KafkaTopicConfig;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CouponEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public CouponEventProducer(
        @Qualifier("couponKafkaTemplate") KafkaTemplate<String, Object> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }


    public void publishCouponUsedEvent(CouponUsedEvent event) {
        String key = event.orderId().toString();

        CompletableFuture<SendResult<String, Object>> future =
            kafkaTemplate.send(KafkaTopicConfig.COUPON_TOPIC, key, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("쿠폰 사용 이벤트 발행 성공: orderId={}, userCouponId={}", event.orderId(),
                    event.userCouponId());
            } else {
                log.error("쿠폰 사용 이벤트 발행 실패: orderId={}, userCouponId={}, error={}", event.orderId(),
                    event.userCouponId(), ex.getMessage());
            }
        });
    }

    public void publishCouponUseFailedEvent(CouponUsedFailedEvent event) {
        String key = event.orderId().toString();

        CompletableFuture<SendResult<String, Object>> future =
            kafkaTemplate.send(KafkaTopicConfig.COUPON_TOPIC, key, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("쿠폰 사용 실패 이벤트 발행 성공: orderId={}, userCouponId={}", event.orderId(),
                    event.userCouponId());
                log.info("쿠폰 사용 이벤트 발행 성공: orderId={}, userCouponId={}", event.orderId(),
                    event.userCouponId());
            } else {
                log.error("쿠폰 사용 이벤트 발행 실패: orderId={}, userCouponId={}, error={}", event.orderId(),
                    event.userCouponId(),
                    ex.getMessage());
            }
        });
    }

    public void publishCouponCancelledEvent(CouponCancelledEvent event) {
        String key = event.orderId().toString();

        CompletableFuture<SendResult<String, Object>> future =
            kafkaTemplate.send(KafkaTopicConfig.COUPON_TOPIC, key, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("쿠폰 취소 이벤트 발행 성공: orderId={}, userCouponId={}", event.orderId(),
                    event.userCouponId());
            } else {
                log.error("쿠폰 사용 실패 이벤트 발행 실패: orderId={}, userCouponId={}, error={}",
                    event.orderId(), event.userCouponId(), ex.getMessage());
                log.error("쿠폰 취소 이벤트 발행 실패: orderId={}, userCouponId={}, error={}", event.orderId(),
                    event.userCouponId(),
                    ex.getMessage());
            }
        });
    }
}

