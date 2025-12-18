package com.klp.order.application.facade;

import com.klp.global.exception.BusinessException;
import com.klp.global.exception.OrderErrorCode;
import com.klp.order.application.command.CancelOrderCommand;
import com.klp.order.application.service.OrderOutboundRequestService;
import com.klp.order.application.service.OrderOutboxEventService;
import com.klp.order.application.service.OrderService;
import com.klp.order.domain.entity.idempotencykey.OperationType;
import com.klp.order.domain.entity.idempotencykey.Target;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.infrastructure.event.event.OrderCancelledEvent;
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

    // 주문 취소 후 재고 증가 이벤트 발행
    // 이 또한 장애 발생 시 트랜잭션 롤백으로 메시지 소실 방지
    @Transactional
    public Order cancelOrder(CancelOrderCommand command) {
        log.info("=== 주문 취소 시작: orderId={} ===", command.orderId());

        try {
            // 1. 주문 취소 처리
            // 주문Id 확인 없으면 예외 처리
            orderService.findById(command.orderId());

            Order order = orderService.cancelOrder(command);
            log.info("주문 취소 완료 - orderId: {}", command.orderId());

            String InventoryIdempotencyKey = orderOutboundRequestService.generateIdempotencyKey(
                order.getOrderId(),
                Target.INVENTORY,
                OperationType.INCREASE
            );

            String DeliveryIdempotencyKey = orderOutboundRequestService.generateIdempotencyKey(
                order.getOrderId(),
                Target.DELIVERY,
                OperationType.CANCEL
            );

            OrderCancelledEvent event = OrderCancelledEvent.from(
                order,
                order.getUserCouponId(),
                InventoryIdempotencyKey,
                DeliveryIdempotencyKey
            );

            // Outbox 저장 실패 시 예외 발생 → 전체 롤백
            orderOutboxEventService.saveEvent(order.getOrderId(),
                "ORDER_CANCELLED", event);

            log.info("=== 주문 취소 완료: orderId={} ===", command.orderId());
            return order;

        } catch (Exception e) {
            log.error("=== 주문 취소 실패 - 전체 롤백: orderId={}, error={} ===",
                command.orderId(), e.getMessage(), e);
            throw new BusinessException(
                OrderErrorCode.ORDER_CREATION_FAILED,
                "주문 취소 중 오류 발생: " + e.getMessage()
            );
        }
    }
}
