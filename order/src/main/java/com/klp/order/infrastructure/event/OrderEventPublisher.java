package com.klp.order.infrastructure.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventPublisher {

    private static final String ORDER_CREATED_TOPIC = "order-created";
    private static final String ORDER_CANCELLED_TOPIC = "order-cancelled";
    private static final String ORDER_DELIVERY_REQUEST_TOPIC = "order.delivery.request";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderCreated(OrderCreatedEvent event) {
        try {
            kafkaTemplate.send(ORDER_CREATED_TOPIC, event.orderId().toString(), event);
            log.info("주문 생성 이벤트 발행 완료: orderId={}", event.orderId());
        } catch (Exception e) {
            log.error("주문 생성 이벤트 발행 실패: orderId={}", event.orderId(), e);
        }
    }

    public void publishOrderCancelled(OrderCancelledEvent event) {
        try {
            kafkaTemplate.send(ORDER_CANCELLED_TOPIC, event.orderId().toString(), event);
            log.info("주문 취소 이벤트 발행 완료: orderId={}", event.orderId());
        } catch (Exception e) {
            log.error("주문 취소 이벤트 발행 실패: orderId={}", event.orderId(), e);
        }
    }

    public void publishDeliveryRequest(OrderDeliveryRequestEvent event) {
        try {
            kafkaTemplate.send(ORDER_DELIVERY_REQUEST_TOPIC, event.orderId().toString(), event);
            log.info("배송 요청 이벤트 발행 완료: orderId={}", event.orderId());
        } catch (Exception e) {
            log.error("배송 요청 이벤트 발행 실패: orderId={}", event.orderId(), e);
        }
    }
}
