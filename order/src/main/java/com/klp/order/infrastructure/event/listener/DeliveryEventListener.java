package com.klp.order.infrastructure.event.listener;

import com.klp.order.application.service.OrderItemService;
import com.klp.order.application.service.OrderService;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.domain.entity.order.OrderStatus;
import com.klp.order.infrastructure.event.DeliveryCreatedEvent;
import com.klp.order.infrastructure.event.DeliveryCreatedEvent.DeliveryItem;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryEventListener {

    private final OrderService orderService;
    private final OrderItemService orderItemService;

    @KafkaListener(
        topics = "delivery.created",
        groupId = "order-service-group"
    )
    @Transactional
    public void handleDeliveryCreated(DeliveryCreatedEvent event) {
        log.info("=== 배송 생성 완료 이벤트 수신: orderId={}, items={} ===",
            event.orderId(), event.items().size());

        try {
            // 1. 주문 조회
            Order order = orderService.findById(event.orderId());
            log.info("주문 조회 완료 - orderId: {}, 현재 상태: {}",
                order.getOrderId(), order.getOrderStatus());

            // 2. 이벤트의 deliveryId를 각 orderItem에 할당
            assignDeliveryIdsToOrderItems(event.items());

            // 3. 주문 상태를 DELIVERY_ASSIGNED로 변경
            order.changeStatus(OrderStatus.DELIVERY_ASSIGNED);

            log.info("=== 배송 생성 이벤트 처리 완료: orderId={}, 상태={} ===",
                event.orderId(), OrderStatus.DELIVERY_ASSIGNED);

        } catch (Exception e) {
            log.error("배송 생성 이벤트 처리 실패: orderId={}", event.orderId(), e);
            // 이벤트 처리 실패 시 재시도를 위해 예외를 다시 던짐
            throw e;
        }
    }

    // 배송 ID 할당
    private void assignDeliveryIdsToOrderItems(List<DeliveryItem> deliveryItems) {
        log.info("deliveryId 할당 시작 - 총 {}개 아이템", deliveryItems.size());

        // 각 orderItem에 deliveryId 할당
        for (DeliveryItem item : deliveryItems) {
            try {
                orderItemService.assignDeliveryId(item.orderItemId(), item.deliveryId());
                log.info("deliveryId 할당 완료 - orderItemId: {}, deliveryId: {}",
                    item.orderItemId(), item.deliveryId());
            } catch (Exception e) {
                log.error("deliveryId 할당 실패 - orderItemId: {}, deliveryId: {}",
                    item.orderItemId(), item.deliveryId(), e);
                throw e;
            }
        }

        log.info("모든 deliveryId 할당 완료 - 총 {}개", deliveryItems.size());
    }
}
