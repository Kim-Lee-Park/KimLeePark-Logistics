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
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
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
@KafkaListener(
    topics = KafkaTopicConfig.PAYMENT_TOPIC,
    groupId = "promotion-service-group",
    containerFactory = "couponKafkaListenerContainerFactory"
)
public class PaymentEventListener {

    private final UserCouponFacade userCouponFacade;
    private final UserCouponService userCouponService;
    private final CouponOutboxEventService couponOutboxEventService;

    @KafkaHandler
    @Transactional
    public void handlePaymentApproved(
        @Payload PaymentApprovedEvent event,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment) {

        log.info("=== 결제 승인 이벤트 수신: orderId={}, userCouponId={}, partition={}, offset={} ===",
            event.orderId(), event.userCouponId(), partition, offset);

        try {
            // 쿠폰 사용 확정
            if (event.userCouponId() != null) {
                UUID userCouponId = event.userCouponId();
                Long userId = event.userId();
                UserCoupon userCoupon = userCouponService.findByUserCouponId(userCouponId);

                // 쿠폰 사용 확정
                userCouponFacade.useUserCoupon(userCoupon.getCouponId(), userId);
            }

            // CouponUsedEvent 생성 (InventoryDeductedFailedEvent 기본 구조 + PaymentApprovedEvent 추가 필드)
            List<CouponUsedEvent.OrderItem> orderItems = event.products().stream()
                .map(product -> new CouponUsedEvent.OrderItem(
                    product.orderItemId(),
                    product.productId(),
                    product.productName(),
                    product.hubId(),
                    product.quantity(),
                    product.unitPrice(),
                    product.totalPrice()
                ))
                .toList();

            // PaymentApprovedEvent에서 InventoryDeductedFailedEvent 기본 필드들 추출
            // TODO: PaymentApprovedEvent에 다음 필드들이 추가되어야 함: email, addressId, userAddressHubId, address, finalOrderPrice, inventoryIdempotencyKey, createdAt
            // 현재는 PaymentApprovedEvent에 없는 필드들은 null 또는 기본값으로 처리
            CouponUsedEvent couponUsedEvent = new CouponUsedEvent(
                // InventoryDeductedFailedEvent 기본 구조
                event.orderId(),
                event.userId(),
                event.supplierId(),
                event.userCouponId(),
                event.email(),
                event.username(),
                event.comment(),
                event.originalPrice(),
                event.couponDiscountPrice(),
                event.gradeDiscountPrice(),
                event.finalOrderPrice(),

                event.addressId(),
                event.userAddressHubId(),
                event.address(),
                event.deliveryLatitude(),
                event.deliveryLongitude(),

                orderItems,
                event.inventoryIdempotencyKey(),
                event.deliveryIdempotencyKey(),
                event.createdAt(),
                event.occurredAt(),
                // PaymentApprovedEvent 추가 필드
                event.paymentId(),
                event.paidAmount(),
                event.paymentMethod(),
                event.paidAt()
            );

            // CouponUsedEvent를 아웃박스에 저장 (트랜잭션 내에서 저장)
            couponOutboxEventService.saveEvent(
                event.orderId(),
                couponUsedEvent
            );
            log.info("쿠폰 사용 확정 및 아웃박스 이벤트 저장 완료: orderId={}, userCouponId={}", event.orderId(),
                event.userCouponId());

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.info("오프셋 커밋 완료: orderId={}, offset={}", event.orderId(), offset);
            }

        } catch (Exception e) {
            log.error("결제 승인 이벤트 처리 실패: orderId={}, partition={}, offset={}",
                event.orderId(), partition, offset, e);

            // 쿠폰 사용 확정 후 아웃박스 저장 실패 시 쿠폰 상태 원복
            if (event.userCouponId() != null) {
                try {
                    UserCoupon userCoupon = userCouponService.findByUserCouponId(
                        event.userCouponId());
                    // USED 상태면 READY로 원복 (아웃박스 저장 실패로 이벤트 발행 실패)
                    if (userCoupon.getStatus() == UserCouponStatus.USED) {
                        userCouponFacade.couponRestored(event.userCouponId());
                        log.info("쿠폰 상태 원복 완료: userCouponId={}, USED -> READY",
                            event.userCouponId());
                    }
                } catch (Exception restoreException) {
                    log.error("쿠폰 상태 원복 실패: userCouponId={}", event.userCouponId(),
                        restoreException);
                }
            }

            couponOutboxEventService.failEvent(event.orderId(), CouponUsedFailedEvent.from(event));
            throw e;
        }
    }

    @KafkaHandler
    @Transactional
    public void handlePaymentCancelled(
        @Payload PaymentCancelledEvent event,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment) {
        log.info("=== 결제 취소 이벤트 수신: orderId={}, userCouponId={}, partition={}, offset={} ===",
            event.orderId(), event.userCouponId(), partition, offset);
        try {

            if (event.userCouponId() != null) {
                UUID userCouponId = event.userCouponId();
                userCouponFacade.couponRestored(userCouponId);
            }

            List<CouponRestoredEvent.CancelledItemDto> orderItems = event.products().stream()
                .map(product -> new CouponRestoredEvent.CancelledItemDto(
                    product.productId(),
                    product.hubId(),
                    product.quantity()
                ))
                .toList();

            CouponRestoredEvent restoredEvent = new CouponRestoredEvent(
                event.paymentId(),
                event.orderId(),
                event.userId(),
                event.userCouponId(),
                event.inventoryIdempotencyKey(),
                event.deliveryIdempotencyKey(),
                event.reason(),
                orderItems,
                event.cancelledAt(),
                event.occurredAt()
            );

            couponOutboxEventService.cancelEvent(
                event.orderId(),
                restoredEvent
            );

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.info("오프셋 커밋 완료: orderId={}, offset={}", event.orderId(), offset);
            }

        } catch (Exception e) {
            log.error("결제 취소 이벤트 처리 실패: orderId={}, partition={}, offset={}",
                event.orderId(), partition, offset, e);

            if (event.userCouponId() != null) {
                UserCoupon userCoupon = userCouponService.findByUserCouponId(event.userCouponId());

                // 선점 해제먼 되고 아웃박스에서 오류났을경우
                if (userCoupon.getStatus() == UserCouponStatus.READY) {
                    userCoupon.reserve();
                }
            }

            throw e;
        }
    }

    @KafkaHandler
    public void handlePaymentFailed(@Payload PaymentFailedEvent event) {
        log.info("결제 실패 이벤트 수신: orderId={}, reason={}", event.orderId(), event.reason());
        if (event.userCouponId() == null) {
            log.info("쿠폰 미사용 결제 실패 이벤트");
            return;
        }
        userCouponService.cancelReserve(event.orderId());
        log.info("쿠폰 선정 취소 완료");
    }

    @KafkaHandler(isDefault = true)
    public void handleUnknown(Object event) {
        log.warn("알 수 없는 이벤트 타입 수신: {}", event.getClass().getSimpleName());
    }
}

