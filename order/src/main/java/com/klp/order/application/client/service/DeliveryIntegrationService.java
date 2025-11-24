package com.klp.order.application.client.service;

import com.klp.order.application.client.DeliveryClient;
import com.klp.order.application.client.dto.delivery.request.CreateDeliveryRequest;
import com.klp.order.application.client.dto.delivery.request.CreateDeliveryRequest.DeliveryOrderItem;
import com.klp.order.application.client.dto.inventory.response.GetProductResponse;
import com.klp.order.application.command.CreateOrderOutboundRequestCommand;
import com.klp.order.application.service.OrderOutboundRequestService;
import com.klp.order.domain.entity.idempotencykey.OperationType;
import com.klp.order.domain.entity.idempotencykey.Target;
import com.klp.order.domain.entity.order.Order;
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
public class DeliveryIntegrationService {

    private final DeliveryClient deliveryClient;
    private final OrderOutboundRequestService orderOutboundRequestService;

    @Transactional
    public void createDelivery(Order order, Map<UUID, GetProductResponse> productInfoMap) {

        String idempotencyKey = orderOutboundRequestService.generateIdempotencyKey(
            order.getOrderId(),
            Target.DELIVERY,
            OperationType.MAKING
        );

        if (checkExistIdempotencyKey(order.getOrderId(), idempotencyKey)) {
            return;
        }

        List<DeliveryOrderItem> deliveryItems = convertToDeliveryItems(order, productInfoMap);

        CreateDeliveryRequest request = new CreateDeliveryRequest(
            order.getOrderId(),
            idempotencyKey,
            order.getSupplierId(),
            order.getCustomerId(),
            deliveryItems
        );

        deliveryClient.createDelivery(request);

        orderOutboundRequestService.save(new CreateOrderOutboundRequestCommand(
            order.getOrderId(),
            idempotencyKey,
            Target.DELIVERY,
            OperationType.MAKING
        ));

        log.info("배송 생성 요청에 성공하였습니다.: {}", order.getOrderId());
    }

    private boolean checkExistIdempotencyKey(UUID orderId, String idempotencyKey) {
        boolean exists = orderOutboundRequestService.existsByIdempotencyKey(
            idempotencyKey);

        if (exists) {
            log.info("해당 주문의 재고 차감 멱등키 존재. orderId: {}", orderId);
        }
        return exists;
    }

    private List<DeliveryOrderItem> convertToDeliveryItems(
        Order order,
        Map<UUID, GetProductResponse> productInfoMap) {

        return order.getOrderItems().stream()
            .map(orderItem -> {
                GetProductResponse productInfo = productInfoMap.get(orderItem.getProductId());
                return new DeliveryOrderItem(
                    orderItem.getOrderItemId(),
                    productInfo.hubId()
                );
            })
            .toList();
    }
}
