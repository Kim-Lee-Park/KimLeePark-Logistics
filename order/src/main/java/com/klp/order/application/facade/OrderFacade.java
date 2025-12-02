package com.klp.order.application.facade;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.order.application.command.CancelOrderCommand;
import com.klp.order.application.command.CreateOrderCommand;
import com.klp.order.application.service.OrderOutboundRequestService;
import com.klp.order.application.service.OrderOutboxEventService;
import com.klp.order.application.service.OrderService;
import com.klp.order.domain.entity.idempotencykey.OperationType;
import com.klp.order.domain.entity.idempotencykey.Target;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.infrastructure.event.event.OrderCancelledEvent;
import com.klp.order.infrastructure.event.event.OrderCreatedEvent;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;
    private final OrderOutboundRequestService orderOutboundRequestService;
    private final OrderOutboxEventService orderOutboxEventService;
    private final ObjectMapper objectMapper;

    // 주문 생성 후 재고 차감 이벤트 발행
    @Transactional
    public Order createOrder(CreateOrderCommand command) {
        log.info("=== 주문 생성 시작 ===");

        // 1. 주문 생성
        Order order = orderService.createOrder(command);
        log.info("주문 생성 완료 - orderId: {}", order.getOrderId());

        // 2. 재고 차감 이벤트 발행
        String idempotencyKey = orderOutboundRequestService.generateIdempotencyKey(
            order.getOrderId(),
            Target.INVENTORY,
            OperationType.DECREASE
        );

        OrderCreatedEvent event = OrderCreatedEvent.from(order, idempotencyKey);
        orderOutboxEventService.saveEvent("ORDER", order.getOrderId(),
            "ORDER_CREATED", event);
        log.info("=== 주문 생성 완료: orderId={} ===", order.getOrderId());
        return order;
    }

    // 주문 취소 후 재고 증감 이벤트 발행
    @Transactional
    public Order cancelOrder(UUID orderId, CancelOrderCommand command) {
        log.info("=== 주문 취소 시작: orderId={} ===", orderId);

        // 1. 주문 취소 처리
        Order order = orderService.cancelOrder(orderId, command);
        log.info("주문 취소 완료 - orderId: {}", orderId);

        // 2. 재고 복구 이벤트 발행
        String idempotencyKey = orderOutboundRequestService.generateIdempotencyKey(
            order.getOrderId(),
            Target.INVENTORY,
            OperationType.INCREASE
        );

        OrderCancelledEvent event = OrderCancelledEvent.from(order, idempotencyKey);
        orderOutboxEventService.saveEvent("ORDER", order.getOrderId(),
            "ORDER_CANCELLED", event);
        log.info("=== 주문 취소 완료: orderId={} ===", orderId);
        return order;
    }


}