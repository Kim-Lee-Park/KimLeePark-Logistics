package com.klp.order.application.client.facade;

import com.klp.common.exception.BusinessException;
import com.klp.order.application.client.service.DeliveryIntegrationService;
import com.klp.order.application.client.service.InventoryIntegrationService;
import com.klp.order.application.command.CreateOrderCommand;
import com.klp.order.application.service.OrderSagaService;
import com.klp.order.application.service.OrderService;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.domain.entity.order.OrderStatus;
import com.klp.order.domain.entity.saga.OrderSaga;
import com.klp.order.domain.entity.saga.SagaStatus;
import com.klp.order.global.exception.OrderErrorCode;
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

    @Transactional
    public Order createOrder(CreateOrderCommand command) {
        Order order = null;
        OrderSaga saga = null;

        try {
            // Step 0: Saga 시작
            log.info("=== Saga 시작 ===");

            // Step 1: 주문 생성
            log.info("Step 1: 주문 생성 시작");
            order = orderService.createOrder(command);
            log.info("Step 1: 주문 생성 완료 - orderId: {}", order.getOrderId());

            // Saga 생성 및 저장
            saga = orderSagaService.createSaga(order);
            saga.updateStatus(SagaStatus.ORDER_CREATED, 1);
            orderSagaService.save(saga);

            // Step 2: 재고 차감 (OrderItem에서 hubId 직접 사용)
            log.info("Step 2: 재고 차감 시작");
            inventoryIntegrationService.deductInventory(order);
            saga.updateStatus(SagaStatus.INVENTORY_DEDUCTED, 2);
            orderSagaService.save(saga);
            log.info("Step 2: 재고 차감 완료");

            // Step 3: 배송 생성 (OrderItem에서 hubId 직접 사용)
            log.info("Step 3: 배송 생성 시작");
            deliveryIntegrationService.createDelivery(order);
            saga.updateStatus(SagaStatus.DELIVERY_CREATED, 3);
            orderSagaService.save(saga);
            log.info("Step 3: 배송 생성 완료");

            // Step 4: Saga 완료
            saga.updateStatus(SagaStatus.COMPLETED, 4);
            orderSagaService.save(saga);

            // 주문 상태를 DELIVERY_ASSIGNED로 변경
            order.changeStatus(OrderStatus.DELIVERY_ASSIGNED);
            orderService.updateOrder(order.getOrderId(), null);

            log.info("=== Saga 정상 완료: orderId={} ===", order.getOrderId());
            return order;

        } catch (Exception e) {
            log.error("=== Saga 실패 발생 ===", e);

            if (saga != null) {
                saga.recordError(e.getMessage());
                saga.startCompensation();
                orderSagaService.save(saga);

                // 보상 트랜잭션 실행
                executeCompensation(saga, order);
            }

            throw new BusinessException(OrderErrorCode.ORDER_NOT_FOUND);
        }
    }

    // 보상 트랜잭션 실행 실패한 단계부터 역순으로 보상 작업 수행
    private void executeCompensation(OrderSaga saga, Order order) {
        log.warn("=== 보상 트랜잭션 시작: currentStep={} ===", saga.getCurrentStep());

        try {
            Integer failedStep = saga.getCurrentStep();

            // 아래와 같은 로직을 반복하는 이유 => 보상과 관련해서 조금 더 명확하게 로그를 찍고 싶어서 분리
            // Step 3에서 실패: 배송 생성 실패
            // 재고 복구 필요
            if (failedStep >= 2) {
                log.info("보상 Step 2: 재고 복구 시작");
                compensateInventory(order);
                saga.updateStatus(SagaStatus.INVENTORY_COMPENSATION_COMPLETED, failedStep);
                orderSagaService.save(saga);
                log.info("보상 Step 2: 재고 복구 완료");
            }

            // Step 2에서 실패: 재고 차감 실패
            // Step 1에서 실패: 주문 생성 실패
            // 주문 취소 (상태 변경)
            if (failedStep >= 1 && order != null) {
                log.info("보상 Step 1: 주문 취소 시작");
                compensateOrder(order);
                saga.updateStatus(SagaStatus.ORDER_COMPENSATION_COMPLETED, failedStep);
                orderSagaService.save(saga);
                log.info("보상 Step 1: 주문 취소 완료");
            }

            saga.completeCompensation();
            orderSagaService.save(saga);
            log.info("=== 보상 트랜잭션 완료 ===");

        } catch (Exception e) {
            log.error("=== 보상 트랜잭션 실패 ===", e);
            saga.failCompensation(e.getMessage());
            orderSagaService.save(saga);
        }
    }

    // 재고 복구 (보상 트랜잭션)
    private void compensateInventory(Order order) {
        try {
            // 실제로는 InventoryIntegrationService를 통해 재고 복구 API 호출
            log.info("재고 복구 API 호출 필요: orderId={}", order.getOrderId());

            // TODO: 실제 구현 시
            // inventoryIntegrationService.restoreInventory(order);

        } catch (Exception e) {
            log.error("재고 복구 실패: orderId={}", order.getOrderId(), e);
            throw e;
        }
    }

    /**
     * 주문 취소 (보상 트랜잭션)
     */
    private void compensateOrder(Order order) {
        try {
            // 주문 상태를 FAILED로 변경
            order.changeStatus(OrderStatus.FAILED);
            orderService.updateOrder(order.getOrderId(), null);

            log.info("주문 상태를 FAILED로 변경: orderId={}", order.getOrderId());

        } catch (Exception e) {
            log.error("주문 취소 실패: orderId={}", order.getOrderId(), e);
            throw e;
        }
    }
}