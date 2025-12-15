package com.klp.hub.inventory.application.listener;

import com.klp.hub.inventory.application.InventoryFacade;
import com.klp.hub.inventory.domain.event.CouponCancelledEvent;
import com.klp.hub.inventory.domain.event.CouponUsedEvent;
import com.klp.hub.inventory.domain.event.CouponUsedFailedEvent;
import com.klp.hub.inventory.domain.event.InventoryDeductedEvent;
import com.klp.hub.inventory.domain.event.InventoryDeductedEvent.OrderItem;
import com.klp.hub.inventory.domain.event.InventoryReplenishedEvent;
import com.klp.hub.inventory.infrastructure.kafka.config.KafkaTopicConfig;
import com.klp.hub.inventory.infrastructure.kafka.producer.InventoryEventProducer;
import java.time.LocalDateTime;
import java.util.List;
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
    topics = KafkaTopicConfig.COUPON_EVENTS,
    groupId = "inventory-service-group",
    containerFactory = "inventoryKafkaListenerContainerFactory"
)
public class CouponEventListener {

    private final InventoryFacade inventoryFacade;
    private final InventoryEventProducer inventoryEventProducer;

    @KafkaHandler
    @Transactional
    public void handleCouponUsed(
        @Payload CouponUsedEvent event,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment
    ) {
        log.info("쿠폰 사용 이벤트 수신: orderId={}", event.orderId());
        inventoryFacade.confirm(event.orderId());
        try {
            List<OrderItem> orderItems = event.products().stream()
                .map(product -> new InventoryDeductedEvent.OrderItem(
                    product.orderItemId(),
                    product.productId(),
                    product.productName(),
                    product.hubId(),
                    product.quantity(),
                    product.unitPrice(),
                    product.totalPrice()
                )).toList();

            InventoryDeductedEvent inventoryDeductedEvent = new InventoryDeductedEvent(
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

            inventoryEventProducer.publishInventoryDeductedEvent(inventoryDeductedEvent);
            log.info("재고 차감 이벤트 발행 완료: orderId={}", event.orderId());

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.info("오프셋 커밋 완료: orderId={}, offset={}", event.orderId(), offset);
            }

        } catch (Exception e) {
            log.error("재고 차감 이벤트 처리 실패:  orderId={}",
                event.orderId(), e);
            throw e;
        }
    }

    @KafkaHandler
    @Transactional
    public void handleCouponCancelled(
        @Payload CouponCancelledEvent event,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment
    ) {
        log.info("=== 쿠폰 사용 취소 이벤트 수신: orderId={}, userCouponId={}, partition={}, offset={} ===",
            event.orderId(), event.userCouponId(), partition, offset);

        try {
            List<InventoryReplenishedEvent.ProductInfo> orderItems = event.products().stream()
                .map(product -> new InventoryReplenishedEvent.ProductInfo(
                    product.productId(),
                    product.hubId(),
                    product.quantity()
                ))
                .toList();

            InventoryReplenishedEvent inventoryReplenishedEvent = new InventoryReplenishedEvent(
                event.paymentId(),
                event.orderId(),
                event.userId(),
                event.userCouponId(),
                event.inventoryIdempotencyKey(),
                event.deliveryIdempotencyKey(),
                event.reason(),
                orderItems,
                event.cancelledAt(),
                LocalDateTime.now()
            );

            // CouponUsedEvent를 아웃박스에 저장 (트랜잭션 내에서 저장)
            inventoryEventProducer.publishInventoryReplenishedEvent(inventoryReplenishedEvent);
            log.info("재고 복구 및 아웃박스 이벤트 저장 완료: orderId={}, userCouponId={}", event.orderId(),
                event.userCouponId());

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.info("오프셋 커밋 완료: orderId={}, offset={}", event.orderId(), offset);
            }
        } catch (Exception e) {
            log.error("결제 승인 이벤트 처리 실패: orderId={}, partition={}, offset={}",
                event.orderId(), partition, offset, e);
            throw e;
        }

    }


    @KafkaHandler
    public void handleCouponUsedFailed(CouponUsedFailedEvent event) {
        log.info("쿠폰 사용실패 이벤트 수신: orderId={}", event.orderId());
        inventoryFacade.release(event.orderId());
        log.info("재고 선점 해제 완료: orderId={}", event.orderId());
    }

    @KafkaHandler(isDefault = true)
    public void handleUnknown(Object event) {
        log.warn("알 수 없는 이벤트 타입 수신: {}", event.getClass().getSimpleName());
    }
}
