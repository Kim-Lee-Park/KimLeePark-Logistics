package com.klp.order.application.client.facade;

import com.klp.common.exception.BusinessException;
import com.klp.order.application.client.DeliveryClient;
import com.klp.order.application.client.InventoryClient;
import com.klp.order.application.client.dto.delivery.request.CreateDeliveryRequest;
import com.klp.order.application.client.dto.delivery.request.CreateDeliveryRequest.DeliveryOrderItem;
import com.klp.order.application.client.dto.inventory.request.DeductInventoryRequest;
import com.klp.order.application.client.dto.inventory.response.DeductInventoryResponse;
import com.klp.order.application.client.dto.inventory.response.GetProductResponse;
import com.klp.order.application.command.CreateOrderCommand;
import com.klp.order.application.command.OrderItemCommand;
import com.klp.order.application.service.OrderService;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.global.exception.OrderErrorCode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;
    private final InventoryClient inventoryClient;
    private final DeliveryClient deliveryClient;

    @Transactional
    public Order createOrder(CreateOrderCommand command) {
        // 1. 상품별 허브 정보 조회
        Map<UUID, GetProductResponse> productInfoMap = fetchProductInfos(command.items());

        // 2. 재고 차감 (idempotency key 생성)
        String idempotencyKey = generateIdempotencyKey(command);
        deductInventory(idempotencyKey, command.items(), productInfoMap);

        // 3. Order 저장 (기존 OrderService 활용)
        Order order = orderService.createOrder(command);

        // 4. 배송 생성 요청
        createDelivery(order, productInfoMap, idempotencyKey);

        return order;
    }

    private Map<UUID, GetProductResponse> fetchProductInfos(List<OrderItemCommand> items) {
        Map<UUID, GetProductResponse> map = new HashMap<>();
        for (OrderItemCommand item : items) {
            GetProductResponse productInfo = inventoryClient.getProductInfo(item.productId());
            map.put(item.productId(), productInfo);
        }
        return map;
    }

    private void deductInventory(String idempotencyKey,
        List<OrderItemCommand> items,
        Map<UUID, GetProductResponse> productInfoMap) {
        List<DeductInventoryRequest.ProductDeduction> deductions = items.stream()
            .map(item -> {
                GetProductResponse productInfo = productInfoMap.get(item.productId());
                return new DeductInventoryRequest.ProductDeduction(
                    item.productId(),
                    productInfo.hubId(),
                    item.quantity()
                );
            })
            .toList();

        DeductInventoryRequest request = new DeductInventoryRequest(idempotencyKey, deductions);
        DeductInventoryResponse response = inventoryClient.deductInventory(request);

        if (!response.isSuccess() && !response.isAlreadyDeducted()) {
            throw new BusinessException(OrderErrorCode.INVENTORY_DEDUCTION_FAILED);
        }
    }

    private void createDelivery(Order order,
        Map<UUID, GetProductResponse> productInfoMap,
        String idempotencyKey) {
        List<DeliveryOrderItem> deliveryItems = order.getOrderItems().stream()
            .map(orderItem -> {
                GetProductResponse productInfo = productInfoMap.get(orderItem.getProductId());
                return new CreateDeliveryRequest.DeliveryOrderItem(
                    orderItem.getOrderItemId(),
                    productInfo.hubId()
                );
            })
            .toList();

        CreateDeliveryRequest request = new CreateDeliveryRequest(
            order.getOrderId(),
            idempotencyKey,
            order.getSupplierId(),
            order.getCustomerId(),
            deliveryItems
        );

        deliveryClient.createDelivery(request);
    }

    private String generateIdempotencyKey(CreateOrderCommand command) {
        // supplierId + customerId + timestamp 등으로 생성
        return UUID.randomUUID().toString();
    }
}