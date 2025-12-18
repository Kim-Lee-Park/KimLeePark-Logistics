package com.klp.order.application.facade;

import com.klp.global.exception.BusinessException;
import com.klp.global.exception.OrderErrorCode;
import com.klp.order.application.command.CreateOrderCommand;
import com.klp.order.application.command.OrderItemCommand;
import com.klp.order.application.query.ProductQueryService;
import com.klp.order.application.query.UserQueryService;
import com.klp.order.application.service.InventoryClient;
import com.klp.order.application.service.OrderFacadeService;
import com.klp.order.application.service.PromotionClient;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.domain.vo.UserAddress;
import com.klp.order.domain.vo.UserProfile;
import com.klp.order.infrastructure.client.dto.inventory.request.InventoryReservationRequest;
import com.klp.order.infrastructure.client.dto.inventory.response.InventoryReservationResponse;
import com.klp.order.infrastructure.client.dto.promotion.request.PromotionCalculateRequest;
import com.klp.order.infrastructure.client.dto.promotion.response.PromotionResponse;
import com.klp.order.infrastructure.event.event.OrderFailedEvent;
import com.klp.order.infrastructure.event.event.WhichRollback;
import com.klp.order.presentation.dto.result.OrderCreateWithKey;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreateOrchestrator {

    private final OrderFacadeService orderFacadeService;
    private final UserQueryService userQueryService;
    private final ProductQueryService productQueryService;
    private final PromotionClient promotionClient;
    private final InventoryClient inventoryClient;

    public Order createOrder(CreateOrderCommand command) {
        log.info("=== 주문 생성 프로세스 시작: userId={} ===", command.userId());

        OrderCreateWithKey orderAndIdempotencyKey = null;
        boolean inventoryReserved = false;
        boolean promotionApplied = false;

        try {
            UserProfile userProfile = userQueryService.getUserProfile(command.userId());
            UserAddress userAddress = userQueryService.getUserAddress(command.addressId());

            int originalPriceTotal = validateAndCalculatePrice(command.orderItems());

            log.info("Order 생성 트랜잭션 시작");
            orderAndIdempotencyKey = orderFacadeService.createOrder(command);

            log.info("Order 생성 완료 - orderId={}",
                orderAndIdempotencyKey.order().getOrderId());

            log.info("재고 선점 시작");
            InventoryReservationResponse inventoryResponse = reserveInventory(
                orderAndIdempotencyKey, command.orderItems());
            if (inventoryResponse.reserved()) {
                inventoryReserved = true;
            }
            log.info("재고 선점 성공");

            log.info("프로모션 계산 시작");
            PromotionCalculateRequest calculateRequest = new PromotionCalculateRequest(
                command.userCouponId(),
                userProfile.grade(),
                originalPriceTotal
            );
            PromotionResponse promotionResponse = promotionClient.getPromotionInfo(
                calculateRequest);
            promotionApplied = true;

            log.info("할인가 계산 완료 - 최종금액={}", promotionResponse.orderPrice());

            Order finalOrder = orderFacadeService.updateOrderAndPublishEvent(
                orderAndIdempotencyKey.order().getOrderId(),
                promotionResponse,
                userProfile,
                userAddress,
                orderAndIdempotencyKey.inventoryIdempotencyKey(),
                orderAndIdempotencyKey.deliveryIdempotencyKey()
            );

            log.info("=== 주문 생성 프로세스 완료: orderId={} ===", finalOrder.getOrderId());
            return finalOrder;

        } catch (Exception e) {
            log.error("=== 주문 생성 프로세스 실패: {} ===", e.getMessage(), e);

            if (orderAndIdempotencyKey != null && orderAndIdempotencyKey.order() != null) {
                WhichRollback rollbackType = determineRollbackType(
                    inventoryReserved,
                    promotionApplied
                );

                log.info("보상 트랜잭션 타입 결정: {}", rollbackType);

                if (rollbackType != WhichRollback.NONE) {
                    try {
                        OrderFailedEvent failedEvent = new OrderFailedEvent(
                            orderAndIdempotencyKey.order().getOrderId(),
                            command.userCouponId(),
                            rollbackType
                        );

                        orderFacadeService.markOrderAsFailed(
                            orderAndIdempotencyKey.order().getOrderId(),
                            failedEvent,
                            e.getMessage()
                        );
                        log.info("보상 트랜잭션 완료: Order FAILED 처리 및 실패 이벤트 발행 (type: {})",
                            rollbackType);
                    } catch (Exception compensationError) {
                        log.error("보상 트랜잭션 실패: {}", compensationError.getMessage());
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

    private int validateAndCalculatePrice(List<OrderItemCommand> orderItems) {
        int originalPriceTotal = 0;

        for (OrderItemCommand orderItem : orderItems) {
            productQueryService.getProductById(orderItem.productId());
            originalPriceTotal += orderItem.getTotalPrice();
        }

        return originalPriceTotal;
    }

    private InventoryReservationResponse reserveInventory(
        OrderCreateWithKey orderAndIdempotencyKey,
        List<OrderItemCommand> orderItems
    ) {
        List<InventoryReservationRequest.ReservationItemRequest> reservationItems =
            new ArrayList<>();

        for (OrderItemCommand orderItem : orderItems) {
            reservationItems.add(
                new InventoryReservationRequest.ReservationItemRequest(
                    orderItem.productId(),
                    orderItem.hubId(),
                    orderItem.quantity()
                )
            );
        }

        InventoryReservationRequest reservationRequest = new InventoryReservationRequest(
            orderAndIdempotencyKey.order().getOrderId(),
            orderAndIdempotencyKey.inventoryIdempotencyKey(),
            reservationItems
        );

        InventoryReservationResponse inventoryResponse =
            inventoryClient.reserveProduct(reservationRequest);

        validateInventoryReservation(
            inventoryResponse,
            orderAndIdempotencyKey.order().getOrderId(),
            reservationItems.size()
        );

        return inventoryResponse;
    }

    private void validateInventoryReservation(
        InventoryReservationResponse response,
        UUID orderId,
        int itemCount
    ) {
        if (response == null) {
            throw new BusinessException(
                OrderErrorCode.INVENTORY_SERVICE_UNAVAILABLE,
                "재고 서비스에 접근할 수 없습니다."
            );
        }

        if (!response.reserved()) {
            log.warn("재고 선점 실패 - orderId: {}, reason: {}", orderId, response.message());
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
