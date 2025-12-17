package com.klp.order.application.service;

import com.klp.global.exception.BusinessException;
import com.klp.global.exception.OrderErrorCode;
import com.klp.order.application.command.CreateOrderCommand;
import com.klp.order.domain.entity.idempotencykey.OperationType;
import com.klp.order.domain.entity.idempotencykey.Target;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.domain.repository.OrderRepository;
import com.klp.order.domain.vo.UserAddressHubId;
import com.klp.order.domain.vo.UserProfile;
import com.klp.order.infrastructure.client.dto.promotion.response.PromotionResponse;
import com.klp.order.infrastructure.event.event.OrderCreatedEvent;
import com.klp.order.presentation.dto.result.OrderCreateWithKey;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderFacadeService {

    private final OrderRepository orderRepository;
    private final OrderOutboundRequestService orderOutboundRequestService;
    private final OrderOutboxEventService orderOutboxEventService;

    // 이 트랜잭션은 DB 접근 위주의 트랜잭션
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
        UserAddressHubId userAddressHubId,
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
            userAddressHubId.address(),
            inventoryIdempotencyKey,
            deliveryIdempotencyKey,
            userAddressHubId.userAddressHubId()
        );

        orderOutboxEventService.saveEvent(
            order.getOrderId(),
            "ORDER_CREATED",
            event
        );

        log.info("주문 생성 이벤트 아웃박스에 발행 완료 orderId={}", order.getOrderId());
        return order;
    }

    @Transactional
    public void markOrderAsFailed(UUID orderId, String reason) {
        log.warn("=== 보상 트랜잭션: Order FAILED 처리 - orderId={}, reason={} ===",
            orderId, reason);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

        order.changeStatus(com.klp.order.domain.entity.order.OrderStatus.FAILED);
        orderRepository.save(order);

        log.info("Order FAILED 상태 변경 완료 - orderId={}", orderId);
    }

}
