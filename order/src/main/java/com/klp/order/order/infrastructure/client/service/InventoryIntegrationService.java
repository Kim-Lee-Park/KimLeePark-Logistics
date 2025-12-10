package com.klp.order.order.infrastructure.client.service;

import com.klp.order.common.exception.ExternalApiErrorCode;
import com.klp.order.common.exception.ExternalApiException;
import com.klp.order.global.exception.BusinessException;
import com.klp.order.global.exception.OrderErrorCode;
import com.klp.order.order.application.command.CreateOrderOutboundRequestCommand;
import com.klp.order.order.application.service.OrderOutboundRequestService;
import com.klp.order.order.domain.entity.idempotencykey.OperationType;
import com.klp.order.order.domain.entity.idempotencykey.Target;
import com.klp.order.order.domain.entity.order.Order;
import com.klp.order.order.infrastructure.client.InventoryClient;
import com.klp.order.order.infrastructure.client.dto.inventory.request.AllocationsProductRequest;
import com.klp.order.order.infrastructure.client.dto.inventory.request.DeductInventoryRequest;
import com.klp.order.order.infrastructure.client.dto.inventory.request.DeductInventoryRequest.ProductDeduction;
import com.klp.order.order.infrastructure.client.dto.inventory.request.ReplenishInventoryRequest;
import com.klp.order.order.infrastructure.client.dto.inventory.request.ReplenishInventoryRequest.ProductReplenishment;
import com.klp.order.order.infrastructure.client.dto.inventory.response.AllocationsProductResponse;
import com.klp.order.order.infrastructure.client.dto.inventory.response.DeductInventoryResponse;
import com.klp.order.order.infrastructure.client.dto.inventory.response.ReplenishInventoryResponse;
import feign.FeignException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryIntegrationService {

    private final InventoryClient inventoryClient;
    private final OrderOutboundRequestService orderOutboundRequestService;

    public AllocationsProductResponse allocateProduct(AllocationsProductRequest request) {
        try {
            return inventoryClient.allocateProduct(request);
        } catch (FeignException.NotFound e) {
            return null;
        } catch (FeignException e) {
            log.error("[InventoryClient] 재고 선점 호출 중 오류 - productId: {}, status: {}, message: {}",
                request.productId(), e.status(), e.getMessage());
            throw new ExternalApiException(ExternalApiErrorCode.INVENTORY_SERVICE_UNAVAILABLE);
        }
    }

    @Transactional
    public void deductInventory(Order order) {
        String idempotencyKey = orderOutboundRequestService.generateIdempotencyKey(
            order.getOrderId(),
            Target.INVENTORY,
            OperationType.DECREASE
        );

        if (checkExistIdempotencyKey(order.getOrderId(), idempotencyKey)) {
            return;
        }

        List<ProductDeduction> deductions = convertToProductDeductions(order);

        DeductInventoryRequest request = new DeductInventoryRequest(idempotencyKey, deductions);
        DeductInventoryResponse response = inventoryClient.deductInventory(request);

        validateDeductionResponse(response);

        orderOutboundRequestService.save(new CreateOrderOutboundRequestCommand(
            order.getOrderId(),
            idempotencyKey,
            Target.INVENTORY,
            OperationType.DECREASE
        ));

        log.info("주문에 대한 재고 차감에 성공하였습니다. orderId: {}", order.getOrderId());
    }

    @Transactional
    public void replenishInventory(Order order) {
        String idempotencyKey = orderOutboundRequestService.generateIdempotencyKey(
            order.getOrderId(),
            Target.INVENTORY,
            OperationType.INCREASE
        );

        // 이미 복구 처리된 경우 스킵
        if (checkExistIdempotencyKey(order.getOrderId(), idempotencyKey)) {
            log.info("이미 재고 복구가 완료된 주문입니다. orderId: {}", order.getOrderId());
            return;
        }

        List<ProductReplenishment> replenishments = convertToProductReplenishments(order);

        ReplenishInventoryRequest request = new ReplenishInventoryRequest(idempotencyKey,
            replenishments);

        try {
            ReplenishInventoryResponse response = inventoryClient.replenishInventory(request);

            orderOutboundRequestService.save(new CreateOrderOutboundRequestCommand(
                order.getOrderId(),
                idempotencyKey,
                Target.INVENTORY,
                OperationType.INCREASE
            ));

            log.info("주문에 대한 재고 복구에 성공하였습니다. orderId: {}, productIds: {}",
                order.getOrderId(), response.productIds());
        } catch (Exception e) {
            log.error("재고 복구 실패. orderId: {}", order.getOrderId(), e);
            throw e;
        }
    }

    private boolean checkExistIdempotencyKey(UUID orderId, String idempotencyKey) {
        boolean exists = orderOutboundRequestService.existsByIdempotencyKey(idempotencyKey);

        if (exists) {
            log.info("해당 주문의 재고 차감 멱등키 존재. orderId: {}", orderId);
        }
        return exists;
    }

    private List<ProductDeduction> convertToProductDeductions(Order order) {
        return order.getOrderItems().stream()
            .map(orderItem -> new ProductDeduction(
                orderItem.getProductId(),
                orderItem.getHubId(),
                orderItem.getQuantity()
            ))
            .toList();
    }

    private List<ProductReplenishment> convertToProductReplenishments(Order order) {
        return order.getOrderItems().stream()
            .map(orderItem -> new ProductReplenishment(
                orderItem.getProductId(),
                orderItem.getHubId(),
                orderItem.getQuantity()
            ))
            .toList();
    }

    private void validateDeductionResponse(DeductInventoryResponse response) {
        if (!response.isSuccess() && !response.isAlreadyDeducted()) {
            throw new BusinessException(OrderErrorCode.INVENTORY_DEDUCTION_FAILED);
        }
    }
}
