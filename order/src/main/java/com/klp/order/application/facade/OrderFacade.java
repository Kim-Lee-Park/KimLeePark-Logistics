package com.klp.order.application.facade;

import com.klp.order.application.command.CancelOrderCommand;
import com.klp.order.application.command.CreateOrderCommand;
import com.klp.order.application.command.OrderItemCommand;
import com.klp.order.application.query.ProductQueryService;
import com.klp.order.application.query.UserQueryService;
import com.klp.order.application.service.OrderOutboundRequestService;
import com.klp.order.application.service.OrderOutboxEventService;
import com.klp.order.application.service.OrderService;
import com.klp.order.application.service.UserClient;
import com.klp.order.domain.entity.idempotencykey.OperationType;
import com.klp.order.domain.entity.idempotencykey.Target;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.global.exception.BusinessException;
import com.klp.order.global.exception.OrderErrorCode;
import com.klp.order.infrastructure.client.service.InventoryIntegrationService;
import com.klp.order.infrastructure.client.service.PromotionDiscountService;
import com.klp.order.infrastructure.event.event.OrderCancelledEvent;
import com.klp.order.infrastructure.event.event.OrderCreatedEvent;
import java.util.List;
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
    private final UserQueryService userQueryService;
    private final ProductQueryService productQueryService;
    private final PromotionDiscountService promotionService;
    private final InventoryIntegrationService inventoryService;
    private final UserClient userClient;

    @Transactional
    public Order createOrder(CreateOrderCommand command) {

        try {
            // 1. 유저 조회_ 정보 얻기
            //grade , email 뽑아오기
//            UserProfile userProfile = userQueryService.getUserProfile(command.userId());
            //2. userAddressHubId, address  받아오기
//            UserAddressHubId userAddressHubId = userClient.getUserAddressHubIdByAddressId(
//                command.addressId());

            // 3. 주문 생성
            Order order = orderService.createOrder(command);
            log.info("주문 생성 완료 - orderId: {}", order.getOrderId());

            // 4. 상품 존재 확인
            List<OrderItemCommand> orderItems = command.items();
            int originalPriceTotal = 0;
            for (OrderItemCommand orderItem : orderItems) {
                // 돌아가며 상품이 진짜 존재하는지 확인 + 가격 계산
                // 없으면 예외 발생
                originalPriceTotal += orderItem.getTotalPrice();

                // 재고 선점
//                inventoryService.allocateProduct(
//                    new AllocationsProductRequest(
//                        orderItem.productId(),
//                        orderItem.quantity()
//                    )
//                );
//                productQueryService.getProductById(orderItem.productId());
            }

            // 5. 할인 금액 조회 // 추후 사용 예정
//            PromotionCalculateRequest calculateRequest = new PromotionCalculateRequest(
//                userProfile.grade(), originalPriceTotal, command.userCouponId());
//            PromotionResponse promotionResponse = promotionService.promotionInfo(calculateRequest);

            // 추후에 PromotionResponse 값을 사용할 예정
            orderService.updateDiscountPrice(
                order,
                0,
                1,
                originalPriceTotal - 1
            );

            //6. 이벤트 생성
            String InventoryIdempotencyKey = orderOutboundRequestService.generateIdempotencyKey(
                order.getOrderId(),
                Target.INVENTORY,
                OperationType.DECREASE
            );

            String DeliveryIdempotencyKey = orderOutboundRequestService.generateIdempotencyKey(
                order.getOrderId(),
                Target.DELIVERY,
                OperationType.MAKING
            );

            UUID tmpAddressHubId = UUID.randomUUID();
            OrderCreatedEvent event = OrderCreatedEvent.from(order, "email", "address",
                InventoryIdempotencyKey,
                DeliveryIdempotencyKey, tmpAddressHubId);

            // Outbox에 이벤트 저장 시도  실패 시 전체 롤백으로 데이터 일관성을 지키도록 구현
            orderOutboxEventService.saveEvent(order.getOrderId(),
                "ORDER_CREATED", event);

            log.info("=== 주문 생성 완료: orderId={} ===", order.getOrderId());
            return order;


        } catch (Exception e) {
            log.error("=== 주문 생성 실패 - 전체 롤백: {} ===", e.getMessage(), e);
            throw new BusinessException(
                OrderErrorCode.ORDER_CREATION_FAILED,
                "주문 생성 중 오류 발생: " + e.getMessage()
            );
        }
    }

    // 주문 취소 후 재고 증가 이벤트 발행
    // 이 또한 장애 발생 시 트랜잭션 롤백으로 메시지 소실 방지
    @Transactional
    public Order cancelOrder(UUID orderId, CancelOrderCommand command) {
        log.info("=== 주문 취소 시작: orderId={} ===", orderId);

        try {
            // 1. 주문 취소 처리
            // 주문Id 확인 없으면 예외 처리
            orderService.findById(orderId);

            Order order = orderService.cancelOrder(orderId, command);
            log.info("주문 취소 완료 - orderId: {}", orderId);

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

            OrderCancelledEvent event = OrderCancelledEvent.from(order, InventoryIdempotencyKey,
                DeliveryIdempotencyKey);

            // Outbox 저장 실패 시 예외 발생 → 전체 롤백
            orderOutboxEventService.saveEvent(order.getOrderId(),
                "ORDER_CANCELLED", event);

            log.info("=== 주문 취소 완료: orderId={} ===", orderId);
            return order;

        } catch (Exception e) {
            log.error("=== 주문 취소 실패 - 전체 롤백: orderId={}, error={} ===",
                orderId, e.getMessage(), e);
            throw new BusinessException(
                OrderErrorCode.ORDER_CREATION_FAILED,
                "주문 취소 중 오류 발생: " + e.getMessage()
            );
        }
    }
}
