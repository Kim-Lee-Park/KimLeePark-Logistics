package com.klp.order.application.facade;

import com.klp.global.exception.BusinessException;
import com.klp.global.exception.OrderErrorCode;
import com.klp.order.application.command.CancelOrderCommand;
import com.klp.order.application.command.CreateOrderCommand;
import com.klp.order.application.command.OrderItemCommand;
import com.klp.order.application.query.ProductQueryService;
import com.klp.order.application.query.UserQueryService;
import com.klp.order.application.service.InventoryClient;
import com.klp.order.application.service.OrderOutboundRequestService;
import com.klp.order.application.service.OrderOutboxEventService;
import com.klp.order.application.service.OrderService;
import com.klp.order.application.service.PromotionClient;
import com.klp.order.application.service.UserClient;
import com.klp.order.domain.entity.idempotencykey.OperationType;
import com.klp.order.domain.entity.idempotencykey.Target;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.domain.vo.UserAddress;
import com.klp.order.domain.vo.UserProfile;
import com.klp.order.infrastructure.client.dto.inventory.request.InventoryReservationRequest;
import com.klp.order.infrastructure.client.dto.inventory.response.InventoryReservationResponse;
import com.klp.order.infrastructure.client.dto.promotion.request.PromotionCalculateRequest;
import com.klp.order.infrastructure.client.dto.promotion.response.PromotionResponse;
import com.klp.order.infrastructure.event.event.OrderCancelledEvent;
import com.klp.order.infrastructure.event.event.OrderCreatedEvent;
import com.klp.order.infrastructure.event.event.OrderFailedEvent;
import com.klp.order.infrastructure.event.event.WhichRollback;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final PromotionClient promotionClient;
    private final InventoryClient inventoryClient;
    private final UserClient userClient;

    @Transactional
    public Order createOrder(CreateOrderCommand command) {
        Order order = null;
        boolean inventoryReserved = false;
        boolean promotionApplied = false;

        try {
            // 1. 유저 조회_ 정보 얻기
            //grade , email, username 뽑아오기
            UserProfile userProfile = userQueryService.getUserProfile(command.userId());
            //2. userAddressHubId, address  받아오기
            UserAddress userAddress = userQueryService.getUserAddress(command.addressId());

            // 3. 주문 생성
            order = orderService.createOrder(command);
            log.info("주문 생성 완료 - orderId: {}", order.getOrderId());

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
            log.info("멱등키 생성 완료 - orderId: {}", order.getOrderId());

            // 4. 상품 존재 확인
            List<OrderItemCommand> orderItems = command.orderItems();
            int originalPriceTotal = 0;
            List<InventoryReservationRequest.ReservationItemRequest> reservationItems = new ArrayList<>();

            for (OrderItemCommand orderItem : orderItems) {
                // 돌아가며 상품이 진짜 존재하는지 확인 + 가격 계산
                // 없으면 예외 발생
                productQueryService.getProductById(orderItem.productId());
                originalPriceTotal += orderItem.getTotalPrice();
                reservationItems.add(
                    new InventoryReservationRequest.ReservationItemRequest(
                        orderItem.productId(),
                        orderItem.hubId(),
                        orderItem.quantity()
                    )
                );
            }
            // 재고 선점
            InventoryReservationRequest reservationRequest = new InventoryReservationRequest(
                order.getOrderId(),
                InventoryIdempotencyKey,
                reservationItems
            );
            InventoryReservationResponse inventoryResponse = inventoryClient.reserveProduct(
                reservationRequest);

            validateInventoryReservation(
                inventoryResponse,
                order.getOrderId(),
                reservationItems.size()
            );
            if (inventoryResponse.reserved()) {
                inventoryReserved = true;
            }

            // 5. 할인 금액 조회 // 추후 사용 예정
            PromotionCalculateRequest calculateRequest = new PromotionCalculateRequest(
                command.userCouponId(), userProfile.grade(), originalPriceTotal);
            PromotionResponse promotionResponse = promotionClient.getPromotionInfo(
                calculateRequest);

            promotionApplied = true;

            // 추후에 PromotionResponse 값을 사용할 예정
            orderService.updateDiscountPrice(
                order,
                promotionResponse.couponDiscountPrice(),
                promotionResponse.gradeDiscountPrice(),
                promotionResponse.orderPrice()
            );

            //6. 이벤트 생성
            OrderCreatedEvent event = OrderCreatedEvent.from(order, userProfile.email(),
                userProfile.username(), userAddress.address(),
                InventoryIdempotencyKey,
                DeliveryIdempotencyKey, userAddress.userAddressHubId());

            // Outbox에 이벤트 저장 시도  실패 시 전체 롤백으로 데이터 일관성을 지키도록 구현
            orderOutboxEventService.saveEvent(order.getOrderId(),
                "ORDER_CREATED", event);

            log.info("=== 주문 생성 완료: orderId={} ===", order.getOrderId());
            return order;


        } catch (Exception e) {
            log.error("=== 주문 생성 실패 - 전체 롤백: {} ===", e.getMessage(), e);

            if (order != null && order.getOrderId() != null) {
                WhichRollback rollbackType = determineRollbackType(
                    inventoryReserved,
                    promotionApplied
                );

                log.info("보상 트랜잭션 타입 결정: {}", rollbackType);

                if (rollbackType != WhichRollback.NONE) {
                    try {
                        OrderFailedEvent failedEvent = new OrderFailedEvent(
                            order.getOrderId(),
                            command.userCouponId(),
                            rollbackType
                        );

                        orderOutboxEventService.saveFailedEvent(
                            order.getOrderId(),
                            "ORDER_FAILED",
                            failedEvent
                        );
                        log.info("주문 실패 이벤트 발행 완료 - orderId: {}, type: {}",
                            order.getOrderId(), rollbackType);
                    } catch (Exception eventException) {
                        log.error("주문 실패 이벤트 발행 실패 (무시): {}", eventException.getMessage());
                    }
                } else {
                    log.info("선점된 리소스 없음 - 보상 트랜잭션 불필요");
                }
            }

            throw new BusinessException(
                OrderErrorCode.ORDER_CREATION_FAILED,
                "주문 생성 중 오류 발생: " + e.getMessage()
            );
        }
    }

    private WhichRollback determineRollbackType(
        boolean inventoryReserved,
        boolean promotionApplied
    ) {
        if (inventoryReserved && promotionApplied) {
            return WhichRollback.ALL;
        } else if (inventoryReserved) {
            return WhichRollback.INVENTORY;
        } else if (promotionApplied) {
            return WhichRollback.PROMOTION;
        } else {
            return WhichRollback.NONE;
        }
    }

    // 주문 취소 후 재고 증가 이벤트 발행
    // 이 또한 장애 발생 시 트랜잭션 롤백으로 메시지 소실 방지
    @Transactional
    public void cancelOrder(CancelOrderCommand command) {
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
                DeliveryIdempotencyKey,
                command.cancelReason(),
                LocalDateTime.now()
            );

            // Outbox 저장 실패 시 예외 발생 → 전체 롤백
            orderOutboxEventService.saveEvent(order.getOrderId(),
                "ORDER_CANCELLED", event);

            log.info("=== 주문 취소 완료: orderId={} ===", command.orderId());

        } catch (Exception e) {
            log.error("=== 주문 취소 실패 - 전체 롤백: orderId={}, error={} ===",
                command.orderId(), e.getMessage(), e);
            throw new BusinessException(
                OrderErrorCode.ORDER_CREATION_FAILED,
                "주문 취소 중 오류 발생: " + e.getMessage()
            );
        }
    }

    private void validateInventoryReservation(
        InventoryReservationResponse response,
        UUID orderId,
        int itemCount) {

        if (response == null) {
            throw new BusinessException(
                OrderErrorCode.INVENTORY_SERVICE_UNAVAILABLE,
                "재고 서비스에 접근할 수 없습니다."
            );
        }

        if (!response.reserved()) {
            log.warn("재고 선점 실패 - orderId: {}, reason: {}",
                orderId, response.message());
            throw new BusinessException(
                OrderErrorCode.INVENTORY_RESERVATION_FAILED,
                response.message()
            );
        }

        if (response.orderId() == null) {
            log.info("재고 선점 - 이미 처리된 요청 - orderId: {}", orderId);
        } else {
            log.info("재고 선점 성공 - orderId: {}, items: {}", orderId, itemCount);
        }
    }
}
