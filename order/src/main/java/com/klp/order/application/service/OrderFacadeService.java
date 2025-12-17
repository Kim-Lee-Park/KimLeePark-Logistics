package com.klp.order.application.service;

import com.klp.global.exception.BusinessException;
import com.klp.global.exception.OrderErrorCode;
import com.klp.order.application.command.CreateOrderCommand;
import com.klp.order.domain.entity.idempotencykey.OperationType;
import com.klp.order.domain.entity.idempotencykey.Target;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.domain.entity.order.OrderStatus;
import com.klp.order.domain.repository.OrderRepository;
import com.klp.order.domain.vo.UserAddress;
import com.klp.order.domain.vo.UserProfile;
import com.klp.order.infrastructure.client.dto.promotion.response.PromotionResponse;
import com.klp.order.infrastructure.event.event.OrderCreatedEvent;
import com.klp.order.infrastructure.event.event.OrderFailedEvent;
import com.klp.order.presentation.dto.result.OrderCreateWithKey;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderFacadeService {

    private final OrderRepository orderRepository;
    private final OrderOutboundRequestService orderOutboundRequestService;
    private final OrderOutboxEventService orderOutboxEventService;

    @Transactional
    public OrderCreateWithKey createOrder(CreateOrderCommand command) {
        log.info("트랜잭션 시작과 함께 주문 생성 시작");

        Order order = Order.create(
            command.userId(),
            command.userCouponId(),
            command.supplierId(),
            command.comment(),
            command.addressId(),
            command.deliveryLatitude(),
            command.deliveryLongitude(),
            command.orderItems()
        );
        order = orderRepository.save(order);
        log.info("주문생성 완료: orderId = {}", order.getOrderId());

        String inventoryIdempotencyKey = orderOutboundRequestService.generateIdempotencyKey(
            order.getOrderId(),
            Target.INVENTORY,
            OperationType.DECREASE
        );
        String deliveryIdempotencyKey = orderOutboundRequestService.generateIdempotencyKey(
            order.getOrderId(),
            Target.DELIVERY,
            OperationType.MAKING
        );
        log.info("멱등키 생성 완료, orderId={}", order.getOrderId());
        return new OrderCreateWithKey(order, inventoryIdempotencyKey, deliveryIdempotencyKey);
    }

    @Transactional
    public Order updateOrderAndPublishEvent(
        UUID orderId,
        PromotionResponse promotionResponse,
        UserProfile userProfile,
        UserAddress userAddress,
        String inventoryIdempotencyKey,
        String deliveryIdempotencyKey
    ) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

        order.updateDiscountPrice(
            promotionResponse.couponDiscountPrice(),
            promotionResponse.gradeDiscountPrice(),
            promotionResponse.orderPrice()
        );

        order = orderRepository.save(order);
        log.info("할인가 업데이트 완료 - orderId: {}", order.getOrderId());

        OrderCreatedEvent event = OrderCreatedEvent.from(
            order,
            userProfile.email(),
            userProfile.username(),
            userAddress.address(),
            inventoryIdempotencyKey,
            deliveryIdempotencyKey,
            userAddress.userAddressHubId()
        );

        orderOutboxEventService.saveEvent(
            order.getOrderId(),
            "ORDER_CREATED",
            event
        );

        log.info("주문 생성 이벤트 아웃박스에 발행 완료 orderId={}", order.getOrderId());
        return order;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markOrderAsFailed(UUID orderId, OrderFailedEvent failedEvent, String reason) {
        log.warn("=== 보상 트랜잭션 시작: Order FAILED 처리 - orderId={}, rollbackType={}, reason={} ===",
            orderId, failedEvent.type(), reason);

        try {
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

            order.changeStatus(OrderStatus.FAILED);
            orderRepository.save(order);

            log.info("Order FAILED 상태 변경 완료 - orderId={}", orderId);

            publishOrderFailedEvent(failedEvent);

        } catch (Exception e) {
            log.error("보상 트랜잭션 실패: orderId={}, error={}", orderId, e.getMessage(), e);
        }
    }

    private void publishOrderFailedEvent(OrderFailedEvent failedEvent) {
        try {
            orderOutboxEventService.saveFailedEvent(
                failedEvent.orderId(),
                "ORDER_FAILED",
                failedEvent
            );
            log.info("주문 실패 이벤트 발행 완료 - orderId: {}, type: {}",
                failedEvent.orderId(), failedEvent.type());
        } catch (Exception e) {
            log.error("주문 실패 이벤트 발행 실패: orderId={}, error={}",
                failedEvent.orderId(), e.getMessage());
        }
    }
}