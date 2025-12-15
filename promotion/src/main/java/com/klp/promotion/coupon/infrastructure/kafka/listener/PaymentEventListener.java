package com.klp.promotion.coupon.infrastructure.kafka.listener;

import com.klp.promotion.coupon.application.facade.UserCouponFacade;
import com.klp.promotion.coupon.application.service.CouponOutboxEventService;
import com.klp.promotion.coupon.application.service.UserCouponService;
import com.klp.promotion.coupon.domain.entity.UserCoupon;
import com.klp.promotion.coupon.domain.event.CouponUsedEvent;
import com.klp.promotion.coupon.domain.event.CouponUsedFailedEvent;
import com.klp.promotion.coupon.domain.event.PaymentApprovedEvent;
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
    groupId = "coupon-service-group",
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
            // userCouponId가 null이면 쿠폰을 사용하지 않은 주문
            if (event.userCouponId() == null) {
                log.info("쿠폰 미사용 주문: orderId={}", event.orderId());
                if (acknowledgment != null) {
                    acknowledgment.acknowledge();
                }
                return;
            }

            // 쿠폰 사용 확정
            UUID userCouponId = event.userCouponId();
            Long userId = event.userId();

            // UserCoupon 조회하여 couponId 가져오기
            UserCoupon userCoupon =
                userCouponService.findByUserCouponId(userCouponId);

            if (userCoupon == null) {
                log.warn("UserCoupon을 찾을 수 없음: userCouponId={}, orderId={}", userCouponId,
                    event.orderId());
                if (acknowledgment != null) {
                    acknowledgment.acknowledge();
                }
                return;
            }

            // 쿠폰 사용 확정
            userCouponFacade.useUserCoupon(userCoupon.getCouponId(), userId);

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
                userCouponId);

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.info("오프셋 커밋 완료: orderId={}, offset={}", event.orderId(), offset);
            }

        } catch (Exception e) {
            log.error("결제 승인 이벤트 처리 실패: orderId={}, partition={}, offset={}",
                event.orderId(), partition, offset, e);
            // 쿠폰 선점 해제
            userCouponService.cancelReserve(event.userCouponId());
            couponOutboxEventService.failEvent(event.orderId(), CouponUsedFailedEvent.from(event));
            throw e;
        }
    }

    @KafkaHandler(isDefault = true)
    public void handleUnknown(Object event) {
        log.warn("알 수 없는 이벤트 타입 수신: {}", event.getClass().getSimpleName());
    }
}

