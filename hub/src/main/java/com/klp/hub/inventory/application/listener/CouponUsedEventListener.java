package com.klp.hub.inventory.application.listener;

import com.klp.hub.inventory.application.InventoryFacade;
import com.klp.hub.inventory.domain.event.CouponUsedEvent;
import com.klp.hub.inventory.domain.event.InventoryDeductedEvent;
import com.klp.hub.inventory.domain.event.InventoryDeductedEvent.OrderItem;
import com.klp.hub.inventory.infrastructure.kafka.config.KafkaTopicConfig;
import com.klp.hub.inventory.infrastructure.kafka.producer.InventoryEventProducer;
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
public class CouponUsedEventListener {

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
//        inventoryFacade.confirm(event.orderId());
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

    @KafkaHandler(isDefault = true)
    public void handleUnknown(Object event) {
        log.warn("알 수 없는 이벤트 타입 수신: {}", event.getClass().getSimpleName());
    }
}
