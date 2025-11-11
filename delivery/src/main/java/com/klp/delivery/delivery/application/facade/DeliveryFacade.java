package com.klp.delivery.delivery.application.facade;

import com.klp.delivery.common.IdempotencyStatus;
import com.klp.delivery.delivery.application.command.DeliveryCommand;
import com.klp.delivery.delivery.application.command.IdempotencyCommand;
import com.klp.delivery.delivery.application.command.OrderToDeliveryCommand;
import com.klp.delivery.delivery.application.command.OrderToDeliveryCommand.OrderItemCommand;
import com.klp.delivery.delivery.application.service.DeliveryService;
import com.klp.delivery.delivery.application.service.IdempotencyKeyService;
import com.klp.delivery.delivery.domain.Company;
import com.klp.delivery.delivery.domain.Delivery;
import com.klp.delivery.delivery.domain.Driver;
import com.klp.delivery.delivery.presentation.dto.DeliveryResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryFacade {

    private final DeliveryService deliveryService;
    private final IdempotencyKeyService idempotencyKeyService;


    public DeliveryResponse createDelivery(OrderToDeliveryCommand orderCommand, IdempotencyCommand idempotencyCommand) {
        log.info("배송 생성 시작: orderId={}", orderCommand.orderId());

        // 멱등키 등록 (주문 단위로 관리)
        idempotencyKeyService.registerIdempotencyKey(idempotencyCommand);

        try {

            Company company = deliveryService.findCompany(orderCommand.receiverId().toString());

            // TODO: 배송 담당자 api 생성 확정 후 로직 변경
            Driver driver = deliveryService.findDriver(company.hubId());


            // 항목별 배송 생성
            List<DeliveryResponse.DeliveryItemResponse> deliveryItems = createDeliveriesForOrderItems(orderCommand, company, driver);


            IdempotencyCommand updateCommand = new IdempotencyCommand(idempotencyCommand.idempotencyKey(), idempotencyCommand.orderId(), IdempotencyStatus.COMPLETED);

            idempotencyKeyService.updateIdempotencyStatus(updateCommand);

            log.info("배송 생성 완료: orderId={}, deliveryCount={}", orderCommand.orderId(), deliveryItems.size());
            return new DeliveryResponse(deliveryItems);
        } catch (Exception e) {
            log.error("배송 생성 실패: orderId={}, error={}", orderCommand.orderId(), e.getMessage(), e);
            throw e;
        }
    }

    private List<DeliveryResponse.DeliveryItemResponse> createDeliveriesForOrderItems(
        OrderToDeliveryCommand orderCommand, Company company, Driver driver) {

        List<DeliveryResponse.DeliveryItemResponse> deliveryItems = new ArrayList<>();
        UUID arrivalId = UUID.fromString(company.hubId());


        for (OrderItemCommand orderItem : orderCommand.items()) {

            DeliveryCommand deliveryCommand = new DeliveryCommand(
                orderCommand.orderId(),
                orderItem.orderItemId(),
                orderItem.hubId(),
                arrivalId,
                orderCommand.senderId(),
                orderCommand.receiverId(),
                company.name(),
                company.address(),
                driver.receiverSlackId(),
                driver.vendorDrvierId()
            );

            Delivery delivery = deliveryService.registerDelivery(deliveryCommand);

            deliveryItems.add(new DeliveryResponse.DeliveryItemResponse(
                orderItem.orderItemId(),
                delivery.getDeliveryId()
            ));

            // TODO: 각 배송 생성 시 경로 생성 이벤트 발행 (비동기)
            // deliveryService.publishDeliveryCreatedEvent(delivery);
        }

        return deliveryItems;
    }
}

