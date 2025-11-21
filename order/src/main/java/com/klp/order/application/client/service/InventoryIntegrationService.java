package com.klp.order.application.client.service;

import com.klp.common.exception.BusinessException;
import com.klp.order.application.client.InventoryClient;
import com.klp.order.application.client.dto.inventory.request.DeductInventoryRequest;
import com.klp.order.application.client.dto.inventory.response.DeductInventoryResponse;
import com.klp.order.application.client.dto.inventory.response.GetProductResponse;
import com.klp.order.application.command.CreateOrderOutboundRequestCommand;
import com.klp.order.application.command.OrderItemCommand;
import com.klp.order.application.service.OrderOutboundRequestService;
import com.klp.order.domain.entity.idempotencykey.OperationType;
import com.klp.order.domain.entity.idempotencykey.Target;
import com.klp.order.global.exception.OrderErrorCode;
import java.util.List;
import java.util.Map;
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
    public void deductInventory(UUID orderId, List<OrderItemCommand> items,
        Map<UUID, GetProductResponse> productInfoMap) {

        String idempotencyKey = orderOutboundRequestService.generateIdempotencyKey(orderId,
            Target.INVENTORY, OperationType.DECREASE);

        if (checkExistIdempotencyKey(idempotencyKey)) {
            log.info("해당 주문의 재고 차감 멱등키 존재:{}", orderId);
        }

        List<DeductInventoryRequest.ProductDeduction> deductions = items.stream().
            map(item -> {
                GetProductResponse productInfo = productInfoMap.get(item.productId());
                return new DeductInventoryRequest.ProductDeduction(
                    item.productId(),
                    productInfo.hubId(),
                    item.quantity()
                );
            }).toList();

        DeductInventoryRequest request = new DeductInventoryRequest(idempotencyKey, deductions);
        DeductInventoryResponse response = inventoryClient.deductInventory(request);

        if (!response.isSuccess() && !response.isAlreadyDeducted()) {
            throw new BusinessException(OrderErrorCode.INVENTORY_DEDUCTION_FAILED);
        }

        orderOutboundRequestService.save(new CreateOrderOutboundRequestCommand(
            orderId,
            idempotencyKey,
            Target.INVENTORY,
            OperationType.DECREASE
        ));

        log.info("주문에 대한 재고 차감에 성공하였습니다.: {}", orderId);
    }

    private boolean checkExistIdempotencyKey(String idempotencyKey) {
        return orderOutboundRequestService.existsByIdempotencyKey(idempotencyKey);
    }
}
