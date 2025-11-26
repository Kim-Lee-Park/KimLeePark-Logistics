package com.klp.order.application.client.service;

import com.klp.order.application.client.DeliveryClient;
import com.klp.order.application.client.dto.delivery.request.CreateDeliveryRequest;
import com.klp.order.application.client.dto.delivery.request.CreateDeliveryRequest.DeliveryOrderItem;
import com.klp.order.application.command.CreateOrderOutboundRequestCommand;
import com.klp.order.application.service.OrderOutboundRequestService;
import com.klp.order.domain.entity.idempotencykey.OperationType;
import com.klp.order.domain.entity.idempotencykey.Target;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.domain.entity.orderitem.OrderItem;
import java.util.List;
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
    public void createDelivery(Order order) {
        String idempotencyKey = orderOutboundRequestService.generateIdempotencyKey(
            order.getOrderId(),
            Target.DELIVERY,
            OperationType.MAKING
        );

        if (checkExistIdempotencyKey(order.getOrderId(), idempotencyKey)) {
            return;
        }

        List<DeliveryOrderItem> deliveryItems = convertToDeliveryItems(order);

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

    @Transactional
    public void deleteDeliveries(Order order) {
        List<UUID> deliveryIds = order.getOrderItems().stream()
            .map(OrderItem::getDeliveryId)
            .filter(deliveryId -> deliveryId != null)
            .distinct()
            .toList();

        if (deliveryIds.isEmpty()) {
            log.info("삭제할 배송이 없습니다. orderId: {}", order.getOrderId());
            return;
        }

        for (UUID deliveryId : deliveryIds) {
            try {
                deliveryClient.deleteDelivery(deliveryId);
                log.info("배송 삭제 성공: deliveryId={}, orderId={}", deliveryId,
                    order.getOrderId());
            } catch (Exception e) {
                log.error("배송 삭제 실패: deliveryId={}, orderId={}", deliveryId,
                    order.getOrderId(), e);
            }
        }

    }

    private boolean checkExistIdempotencyKey(UUID orderId, String idempotencyKey) {
        boolean exists = orderOutboundRequestService.existsByIdempotencyKey(idempotencyKey);

        if (exists) {
            log.info("해당 주문의 배송 생성 멱등키 존재. orderId: {}", orderId);
        }
        return exists;
    }

    private List<DeliveryOrderItem> convertToDeliveryItems(Order order) {
        return order.getOrderItems().stream()
            .map(orderItem -> new DeliveryOrderItem(
                orderItem.getOrderItemId(),
                orderItem.getHubId()
            ))
            .toList();
    }
}