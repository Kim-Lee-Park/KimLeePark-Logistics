package com.klp.order.infrastructure.client.service.facade;

import com.klp.common.exception.BusinessException;
import com.klp.order.application.command.CreateOrderCommand;
import com.klp.order.application.service.OrderSagaService;
import com.klp.order.application.service.OrderService;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.domain.entity.saga.OrderSaga;
import com.klp.order.domain.entity.saga.SagaStatus;
import com.klp.order.global.exception.OrderErrorCode;
import com.klp.order.infrastructure.client.service.DeliveryIntegrationService;
import com.klp.order.infrastructure.client.service.InventoryIntegrationService;
import com.klp.order.infrastructure.client.service.SagaCompensationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;
    private final OrderSagaService orderSagaService;
    private final InventoryIntegrationService inventoryIntegrationService;
    private final DeliveryIntegrationService deliveryIntegrationService;
    private final SagaCompensationService sagaCompensationService;

    @Transactional
    public Order createOrder(CreateOrderCommand command) {
        Order order = null;
        OrderSaga saga = null;

        try {
            log.info("=== Saga 시작 ===");

            // Step 1: 주문 생성
            log.info("Step 1: 주문 생성 시작");
            order = orderService.createOrder(command);
            log.info("Step 1: 주문 생성 완료 - orderId: {}", order.getOrderId());

            // Saga 생성 및 저장
            saga = orderSagaService.createSaga(order);
            saga.updateStatus(SagaStatus.ORDER_CREATED, 1);
            orderSagaService.save(saga);

            // Step 2: 재고 차감
            log.info("Step 2: 재고 차감 시작");
            inventoryIntegrationService.deductInventory(order);
            saga.updateStatus(SagaStatus.INVENTORY_DEDUCTED, 2);
            orderSagaService.save(saga);
            log.info("Step 2: 재고 차감 완료");

            // Step 3: 배송 생성
            log.info("Step 3: 배송 생성 시작");
            deliveryIntegrationService.createDelivery(order);
            saga.updateStatus(SagaStatus.DELIVERY_CREATED, 3);
            orderSagaService.save(saga);
            log.info("Step 3: 배송 생성 완료");

            // Step 4: Saga 완료
            saga.updateStatus(SagaStatus.COMPLETED, 4);
            orderSagaService.save(saga);

            log.info("=== Saga 정상 완료: orderId={} ===", order.getOrderId());
            return order;

        } catch (Exception e) {
            log.error("=== Saga 실패 발생 ===", e);

            // 별도 서비스에서 보상 처리
            if (saga != null && order != null) {
                sagaCompensationService.handleSagaFailure(
                    saga.getSagaId(),
                    order.getOrderId(),
                    saga.getCurrentStep(),
                    e.getMessage()
                );
            }

            throw new BusinessException(OrderErrorCode.ORDER_CREATION_FAILED);
        }
    }
}