package com.klp.promotion.coupon.infrastructure.kafka.listener;

import com.klp.promotion.coupon.application.facade.UserCouponFacade;
import com.klp.promotion.coupon.application.service.CouponOutboxEventService;
import com.klp.promotion.coupon.application.service.UserCouponService;
import com.klp.promotion.coupon.domain.entity.UserCoupon;
import com.klp.promotion.coupon.domain.enums.UserCouponStatus;
import com.klp.promotion.coupon.domain.event.CouponRestoredEvent;
import com.klp.promotion.coupon.domain.event.CouponUsedEvent;
import com.klp.promotion.coupon.domain.event.CouponUsedFailedEvent;
import com.klp.promotion.coupon.domain.event.PaymentApprovedEvent;
import com.klp.promotion.coupon.domain.event.PaymentCancelledEvent;
import com.klp.promotion.coupon.domain.event.PaymentFailedEvent;
import com.klp.promotion.coupon.infrastructure.kafka.config.KafkaTopicConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final UserCouponFacade userCouponFacade;
    private final UserCouponService userCouponService;
    private final CouponOutboxEventService couponOutboxEventService;

    @KafkaListener(
        topics = KafkaTopicConfig.PAYMENT_APPROVED_TOPIC,
        groupId = "coupon-service-group",
        containerFactory = "couponKafkaListenerContainerFactory"
    )
    @Transactional
    public void handlePaymentApproved(
        @Payload PaymentApprovedEvent event,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment
    ) {
        log.info("=== 결제 승인 수신: orderId={}, userCouponId={}, partition={}, offset={} ===",
            event.orderId(), event.userCouponId(), partition, offset);

        try {
            if (event.userCouponId() != null) {
                UserCoupon userCoupon = userCouponService.findByUserCouponId(event.userCouponId());
                userCouponFacade.useUserCoupon(userCoupon.getCouponId(), event.userId());
            }

            couponOutboxEventService.saveEvent(
                event.orderId(),
                CouponUsedEvent.from(event)
            );

            log.info("쿠폰 사용 확정 및 아웃박스 저장 완료: orderId={}", event.orderId());

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }

        } catch (Exception e) {
            log.error("결제 승인 처리 실패: orderId={}, cause={}", event.orderId(), e.getMessage(), e);

            // 보상 트랜잭션 및 실패 이벤트 처리
            rollbackCouponUsage(event);
            couponOutboxEventService.failEvent(event.orderId(), CouponUsedFailedEvent.from(event));

            throw e;
        }
    }

    @KafkaListener(
        topics = KafkaTopicConfig.PAYMENT_CANCELLED_DLT,
        groupId = "coupon-service-group",
        containerFactory = "couponKafkaListenerContainerFactory"
    )
    @Transactional
    public void handlePaymentCancelled(
        @Payload PaymentCancelledEvent event,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment
    ) {
        log.info("=== 결제 취소 수신: orderId={}, userCouponId={} ===", event.orderId(),
            event.userCouponId());

        try {
            if (event.userCouponId() != null) {
                userCouponFacade.couponRestored(event.userCouponId());
            }
            couponOutboxEventService.cancelEvent(
                event.orderId(),
                CouponRestoredEvent.from(event)
            );

            log.info("쿠폰 복구 및 아웃박스 저장 완료: orderId={}", event.orderId());

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }

        } catch (Exception e) {
            log.error("결제 취소 처리 실패: orderId={}, cause={}", event.orderId(), e.getMessage(), e);
            rollbackCouponRestoration(event);
            throw e;
        }
    }

    @KafkaListener(
        topics = KafkaTopicConfig.PAYMENT_FAILED_TOPIC,
        groupId = "coupon-service-group",
        containerFactory = "couponKafkaListenerContainerFactory"
    )
    public void handlePaymentFailed(@Payload PaymentFailedEvent event) {
        log.info("결제 실패 수신: orderId={}, reason={}", event.orderId(), event.reason());

        if (event.userCouponId() == null) {
            return;
        }

        userCouponService.cancelReserve(event.orderId());
        log.info("쿠폰 선점 취소 완료");
    }


    @KafkaListener(
        topics = KafkaTopicConfig.PAYMENT_APPROVED_DLT,
        groupId = "coupon-service-group-dlt",
        containerFactory = "couponKafkaListenerContainerFactory"
    )
    public void handlePaymentApprovedDlt(@Payload PaymentApprovedEvent event) {
        log.error("========================================");
        log.error("⚠️ DLT 도착: PaymentApproved");
        log.error("⚠️ 결제 처리 실패 - 수동 처리 필요!");
        log.error("========================================");
        log.error("orderId={}", event.orderId());
    }

    @KafkaListener(
        topics = KafkaTopicConfig.PAYMENT_CANCELLED_DLT,
        groupId = "coupon-service-group-dlt",
        containerFactory = "couponKafkaListenerContainerFactory"
    )
    public void handlePaymentCancelledDlt(@Payload PaymentCancelledEvent event) {
        log.error("========================================");
        log.error("⚠️ DLT 도착: PaymentCancelled");
        log.error("⚠️ 결제 취소 처리 실패 - 수동 처리 필요!");
        log.error("========================================");
        log.error("orderId={}", event.orderId());
    }


    @KafkaListener(
        topics = KafkaTopicConfig.PAYMENT_FAILED_DLT,
        groupId = "coupon-service-group-dlt",
        containerFactory = "couponKafkaListenerContainerFactory"
    )
    public void handlePaymentFailedDlt(@Payload PaymentFailedEvent event) {
        log.error("========================================");
        log.error("⚠️ DLT 도착: PaymentFailed");
        log.error("⚠️ 결제 실패 처리 실패 - 수동 처리 필요!");
        log.error("========================================");
        log.error("orderId={}", event.orderId());
    }

    private void rollbackCouponUsage(PaymentApprovedEvent event) {
        if (event.userCouponId() == null) {
            return;
        }

        try {
            UserCoupon userCoupon = userCouponService.findByUserCouponId(event.userCouponId());
            if (userCoupon.getStatus() == UserCouponStatus.USED) {
                userCouponFacade.couponRestored(event.userCouponId());
                log.info("롤백 완료: 쿠폰 상태 원복 (USED -> READY), userCouponId={}", event.userCouponId());
            }
        } catch (Exception ex) {
            log.error("롤백 실패: userCouponId={}", event.userCouponId(), ex);
        }
    }

    private void rollbackCouponRestoration(PaymentCancelledEvent event) {
        if (event.userCouponId() == null) {
            return;
        }

        try {
            UserCoupon userCoupon = userCouponService.findByUserCouponId(event.userCouponId());
            // READY 상태라면 다시 선점(RESERVE) 상태로 되돌림
            if (userCoupon.getStatus() == UserCouponStatus.READY) {
                userCoupon.reserve();
                log.info("롤백 완료: 쿠폰 재선점 (READY -> RESERVED), userCouponId={}",
                    event.userCouponId());
            }
        } catch (Exception ex) {
            log.error("롤백 실패: userCouponId={}", event.userCouponId(), ex);
        }
    }
}