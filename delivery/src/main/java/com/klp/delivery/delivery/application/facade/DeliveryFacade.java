package com.klp.delivery.delivery.application.facade;

import com.klp.delivery.common.IdempotencyStatus;
import com.klp.delivery.delivery.application.command.DeliveryCommand;
import com.klp.delivery.delivery.application.command.IdempotencyCommand;
import com.klp.delivery.delivery.application.command.OrderToDeliveryCommand;
import com.klp.delivery.delivery.application.command.OrderToDeliveryCommand.OrderItemCommand;
import com.klp.delivery.delivery.application.service.DeliveryService;
import com.klp.delivery.delivery.application.service.IdempotencyKeyService;
import com.klp.delivery.delivery.application.command.CompanyCommand;
import com.klp.delivery.delivery.domain.entity.Delivery;
import com.klp.delivery.delivery.application.command.DriverCommand;
import com.klp.delivery.delivery.domain.entity.DeliveryItem;
import com.klp.delivery.delivery.presentation.dto.DeliveryResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryFacade {

    private final DeliveryService deliveryService;
    private final IdempotencyKeyService idempotencyKeyService;


    @Transactional
    public DeliveryResponse createDelivery(OrderToDeliveryCommand orderCommand, IdempotencyCommand idempotencyCommand) {
        log.info("배송 생성 시작: orderId={}", orderCommand.orderId());

        // 멱등키 등록 (주문 단위로 관리)
        idempotencyKeyService.registerIdempotencyKey(idempotencyCommand);

        try {

            CompanyCommand companyCommand = deliveryService.findCompany(orderCommand.receiverId().toString());

            // TODO: 배송 담당자 api 생성 확정 후 로직 변경
            DriverCommand driverCommand = deliveryService.findDriver(companyCommand.hubId());


            // 항목별 배송 생성
            List<DeliveryResponse.DeliveryItemResponse> deliveryItems = createDeliveriesForOrderItems(orderCommand,
                companyCommand, driverCommand);


            IdempotencyCommand updateCommand = new IdempotencyCommand(idempotencyCommand.idempotencyKey(), idempotencyCommand.orderId(), IdempotencyStatus.COMPLETED);

            idempotencyKeyService.updateIdempotencyStatus(updateCommand);

            log.info("배송 생성 완료: orderId={}, deliveryCount={}", orderCommand.orderId(), deliveryItems.size());
            return new DeliveryResponse(orderCommand.orderId(), deliveryItems);
        } catch (Exception e) {
            log.error("배송 생성 실패: orderId={}, error={}", orderCommand.orderId(), e.getMessage(), e);
            throw e;
        }
    }

    private List<DeliveryResponse.DeliveryItemResponse> createDeliveriesForOrderItems(
        OrderToDeliveryCommand orderCommand, CompanyCommand companyCommand, DriverCommand driverCommand) {

        List<DeliveryResponse.DeliveryItemResponse> deliveryItems = new ArrayList<>();
        UUID arrivalId = UUID.fromString(companyCommand.hubId());

        Map<UUID,List<OrderItemCommand>> list = orderCommand.items().stream().collect(Collectors.groupingBy(OrderItemCommand::hubId));

        for(Map.Entry<UUID,List<OrderItemCommand>> entry : list.entrySet()){

            UUID hubId = entry.getKey();
            List<OrderItemCommand> orderItems = entry.getValue();

            DeliveryCommand deliveryCommand = new DeliveryCommand(
                orderCommand.orderId(),
                hubId,
                arrivalId,
                orderCommand.senderId(),
                orderCommand.receiverId(),
                companyCommand.name(),
                companyCommand.address(),
                driverCommand.receiverSlackId(),
                driverCommand.vendorDrvierId()
            );

            Delivery delivery = deliveryService.registerDelivery(deliveryCommand, orderItems);

            for(DeliveryItem item: delivery.getDeliveryItems()){
                deliveryItems.add(new DeliveryResponse.DeliveryItemResponse(
                    item.getOrderItemId(),
                    delivery.getDeliveryId()
                ));

                log.info("OrderId={}, OrderItemId={}, DeliveryId={}", delivery.getOrderId(), item.getOrderItemId(), delivery.getDeliveryId());
            }

            // TODO: 각 배송 생성 시 경로 생성 이벤트 발행 (비동기)
            // deliveryService.publishDeliveryCreatedEvent(delivery);
        }

        return deliveryItems;
    }
}

