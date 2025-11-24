package com.klp.order.application.client.facade;

import com.klp.order.application.client.InventoryClient;
import com.klp.order.application.client.dto.inventory.response.GetProductResponse;
import com.klp.order.application.client.service.DeliveryIntegrationService;
import com.klp.order.application.client.service.InventoryIntegrationService;
import com.klp.order.application.command.CreateOrderCommand;
import com.klp.order.application.service.OrderService;
import com.klp.order.domain.entity.order.Order;
import java.util.HashMap;
import java.util.Map;
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
    private final InventoryClient inventoryClient;
    private final InventoryIntegrationService inventoryIntegrationService;
    private final DeliveryIntegrationService deliveryIntegrationService;

    @Transactional
    public Order createOrder(CreateOrderCommand command) {
        // 1. 상품별 허브 정보 조회
        Map<UUID, GetProductResponse> productInfoMap = fetchProductInfos(command);

        // 2. Order 저장
        Order order = orderService.createOrder(command);
        log.info("주문 생성: {}", order.getOrderId());

        // 3. 재고 차감
        inventoryIntegrationService.deductInventory(
            order.getOrderId(),
            command.items(),
            productInfoMap
        );

        // 4. 배송 생성
        deliveryIntegrationService.createDelivery(order, productInfoMap);

        log.info("주문 생성 관련 로직 성공: {}", order.getOrderId());
        return order;
    }

    private Map<UUID, GetProductResponse> fetchProductInfos(CreateOrderCommand command) {
        Map<UUID, GetProductResponse> productInfoMap = new HashMap<>();

        command.items().forEach(item -> {
            GetProductResponse productInfo = inventoryClient.getProductInfo(item.productId());
            productInfoMap.put(item.productId(), productInfo);
        });

        return productInfoMap;
    }

}