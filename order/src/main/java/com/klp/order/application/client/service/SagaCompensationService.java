package com.klp.order.application.client.service;

import com.klp.order.application.service.OrderSagaService;
import com.klp.order.application.service.OrderService;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.domain.entity.order.OrderStatus;
import com.klp.order.domain.entity.saga.OrderSaga;
import com.klp.order.domain.entity.saga.SagaStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SagaCompensationService {

    private final OrderService orderService;
    private final OrderSagaService orderSagaService;
    private final InventoryIntegrationService inventoryIntegrationService;
    private final DeliveryIntegrationService deliveryIntegrationService;

    /**
     * Saga 실패 처리 - 별도 트랜잭션
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleSagaFailure(UUID sagaId, UUID orderId, Integer failedStep,
        String errorMessage) {
        try {
            // 새 트랜잭션에서 엔티티 재조회
            OrderSaga saga = orderSagaService.findById(sagaId)
                .orElseThrow(() -> new IllegalStateException("Saga not found: " + sagaId));
            Order order = orderService.findById(orderId);

            // 실패 상태 기록
            saga.recordError(errorMessage);
            saga.startCompensation();
            orderSagaService.save(saga);

            log.info("=== Saga 실패 상태 기록 완료, 보상 시작: step={} ===", failedStep);

            // 보상 실행
            executeCompensation(saga, order, failedStep);

            log.info("=== Saga 보상 처리 완료 ===");

        } catch (Exception e) {
            log.error("=== Saga 보상 처리 실패 ===", e);
            try {
                OrderSaga saga = orderSagaService.findById(sagaId).orElse(null);
                if (saga != null) {
                    saga.failCompensation("보상 처리 실패: " + e.getMessage());
                    orderSagaService.save(saga);
                }
            } catch (Exception finalError) {
                log.error("=== 최종 상태 저장 실패 ===", finalError);
            }
        }
    }

    /**
     * 보상 트랜잭션 실행 - 성공한 단계만 역순으로 보상
     */
    private void executeCompensation(OrderSaga saga, Order order, Integer failedStep) {
        log.warn("=== 보상 트랜잭션 시작: failedStep={} ===", failedStep);

        List<String> compensationErrors = new ArrayList<>();

        try {
            // Step 3 성공 → 배송 생성됨 → 배송 삭제 필요
            if (failedStep > 3) {
                log.info("보상 Step 3: 배송 삭제 시작");
                try {
                    deliveryIntegrationService.deleteDeliveries(order);
                    saga.updateStatus(SagaStatus.DELIVERY_COMPENSATION_COMPLETED, failedStep);
                    orderSagaService.save(saga);
                    log.info("보상 Step 3: 배송 삭제 완료");
                } catch (Exception e) {
                    log.error("배송 삭제 실패", e);
                    compensationErrors.add("배송 삭제 실패: " + e.getMessage());
                }
            }

            // Step 2 성공 → 재고 차감됨 → 재고 복구 필요
            if (failedStep > 2) {
                log.info("보상 Step 2: 재고 복구 시작");
                try {
                    inventoryIntegrationService.replenishInventory(order);
                    saga.updateStatus(SagaStatus.INVENTORY_COMPENSATION_COMPLETED, failedStep);
                    orderSagaService.save(saga);
                    log.info("보상 Step 2: 재고 복구 완료");
                } catch (Exception e) {
                    log.error("재고 복구 실패", e);
                    compensationErrors.add("재고 복구 실패: " + e.getMessage());
                }
            }

            // Step 1 성공 → 주문 생성됨 → 주문 실패 처리 (항상 실행)
            log.info("보상 Step 1: 주문 실패 처리 시작");
            try {
                order.changeStatus(OrderStatus.FAILED);
                orderService.updateOrder(order.getOrderId(), null);
                saga.updateStatus(SagaStatus.ORDER_COMPENSATION_COMPLETED, failedStep);
                orderSagaService.save(saga);
                log.info("보상 Step 1: 주문 실패 처리 완료");
            } catch (Exception e) {
                log.error("주문 실패 처리 실패", e);
                compensationErrors.add("주문 실패 처리 실패: " + e.getMessage());
            }

            // 최종 상태 업데이트
            if (compensationErrors.isEmpty()) {
                saga.completeCompensation();
                orderSagaService.save(saga);
                log.info("=== 모든 보상 완료 ===");
            } else {
                String errorMsg = String.join(", ", compensationErrors);
                saga.failCompensation(errorMsg);
                orderSagaService.save(saga);
                log.error("=== 일부 보상 실패: {} ===", errorMsg);
            }

        } catch (Exception e) {
            log.error("=== 보상 트랜잭션 실패 ===", e);
            saga.failCompensation(e.getMessage());
            orderSagaService.save(saga);
        }
    }
}
