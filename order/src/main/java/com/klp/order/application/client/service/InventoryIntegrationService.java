package com.klp.order.application.client.service;

import com.klp.common.exception.BusinessException;
import com.klp.order.application.client.InventoryClient;
import com.klp.order.application.client.dto.inventory.request.DeductInventoryRequest;
import com.klp.order.application.client.dto.inventory.request.DeductInventoryRequest.ProductDeduction;
import com.klp.order.application.client.dto.inventory.response.DeductInventoryResponse;
import com.klp.order.application.command.CreateOrderOutboundRequestCommand;
import com.klp.order.application.service.OrderOutboundRequestService;
import com.klp.order.domain.entity.idempotencykey.OperationType;
import com.klp.order.domain.entity.idempotencykey.Target;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.global.exception.OrderErrorCode;
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

    private void validateDeductionResponse(DeductInventoryResponse response) {
        if (!response.isSuccess() && !response.isAlreadyDeducted()) {
            throw new BusinessException(OrderErrorCode.INVENTORY_DEDUCTION_FAILED);
        }
    }
}