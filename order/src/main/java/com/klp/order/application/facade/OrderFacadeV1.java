//package com.klp.order.application.facade;
//
//import com.klp.common.exception.BusinessException;
//import com.klp.order.application.command.CancelOrderCommand;
//import com.klp.order.application.command.CreateOrderCommand;
//import com.klp.order.application.service.OrderOutboundRequestService;
//import com.klp.order.application.service.OrderOutboxEventService;
//import com.klp.order.application.service.OrderService;
//import com.klp.order.domain.entity.idempotencykey.OperationType;
//import com.klp.order.domain.entity.idempotencykey.Target;
//import com.klp.order.domain.entity.order.Order;
//import com.klp.order.global.exception.OrderErrorCode;
//import com.klp.order.infrastructure.event.event.OrderCancelledEvent;
//import com.klp.order.infrastructure.event.event.OrderCreatedEvent;
//import java.util.UUID;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class OrderFacadeV1 {
//
//    private final OrderService orderService;
//    private final OrderOutboundRequestService orderOutboundRequestService;
//    private final OrderOutboxEventService orderOutboxEventService;
//
//    // 주문 생성 후 재고 차감 이벤트 발행
//    // DB 장애 시 전체 트랜잭션 롤백으로 메시지 소실 방지
//    @Transactional
//    public Order createOrder(CreateOrderCommand command) {
//        log.info("=== 주문 생성 시작 ===");
//
//        try {
//            // 1. 주문 생성
//            Order order = orderService.createOrder(command);
//            log.info("주문 생성 완료 - orderId: {}", order.getOrderId());
//
//            String idempotencyKey = orderOutboundRequestService.generateIdempotencyKey(
//                order.getOrderId(),
//                Target.INVENTORY,
//                OperationType.DECREASE
//            );
//
//            OrderCreatedEvent event = OrderCreatedEvent.from(order, idempotencyKey);
//
//            // Outbox에 이벤트 저장 시도  실패 시 전체 롤백으로 데이터 일관성을 지키도록 구현
//            orderOutboxEventService.saveEvent(order.getOrderId(),
//                "ORDER_CREATED", event);
//
//            log.info("=== 주문 생성 완료: orderId={} ===", order.getOrderId());
//            return order;
//
//        } catch (Exception e) {
//            // 어떤 단계에서든 실패하면 전체 롤백
//            log.error("=== 주문 생성 실패 - 전체 롤백: {} ===", e.getMessage(), e);
//            throw new BusinessException(
//                OrderErrorCode.ORDER_CREATION_FAILED,
//                "주문 생성 중 오류 발생: " + e.getMessage()
//            );
//        }
//    }
//
//
//    // 주문 취소 후 재고 증가 이벤트 발행
//    // 이 또한 장애 발생 시 트랜잭션 롤백으로 메시지 소실 방지
//    @Transactional
//    public Order cancelOrder(UUID orderId, CancelOrderCommand command) {
//        log.info("=== 주문 취소 시작: orderId={} ===", orderId);
//
//        try {
//            // 1. 주문 취소 처리
//            Order order = orderService.cancelOrder(orderId, command);
//            log.info("주문 취소 완료 - orderId: {}", orderId);
//
//            String idempotencyKey = orderOutboundRequestService.generateIdempotencyKey(
//                order.getOrderId(),
//                Target.INVENTORY,
//                OperationType.INCREASE
//            );
//
//            OrderCancelledEvent event = OrderCancelledEvent.from(order, idempotencyKey);
//
//            // Outbox 저장 실패 시 예외 발생 → 전체 롤백
//            orderOutboxEventService.saveEvent(order.getOrderId(),
//                "ORDER_CANCELLED", event);
//
//            log.info("=== 주문 취소 완료: orderId={} ===", orderId);
//            return order;
//
//        } catch (Exception e) {
//            log.error("=== 주문 취소 실패 - 전체 롤백: orderId={}, error={} ===",
//                orderId, e.getMessage(), e);
//            throw new BusinessException(
//                OrderErrorCode.ORDER_CREATION_FAILED,
//                "주문 취소 중 오류 발생: " + e.getMessage()
//            );
//        }
//    }
//}