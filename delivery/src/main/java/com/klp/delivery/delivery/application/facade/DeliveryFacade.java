package com.klp.delivery.delivery.application.facade;

import static com.klp.delivery.delivery.exception.DeliveryErrorCode.DELIVERY_CREATION_FAILED;

import com.klp.delivery.common.enums.CustomerDeliveryStatus;
import com.klp.delivery.common.enums.IdempotencyStatus;
import com.klp.delivery.delivery.application.command.DeliveryCommand;
import com.klp.delivery.delivery.application.command.DriverCommand;
import com.klp.delivery.delivery.application.command.IdempotencyCommand;
import com.klp.delivery.delivery.application.command.OrderToDeliveryCommand;
import com.klp.delivery.delivery.application.command.OrderToDeliveryCommand.OrderItemCommand;
import com.klp.delivery.delivery.application.service.DeliveryService;
import com.klp.delivery.delivery.application.event.DeliveryEventPublisher;
import com.klp.delivery.delivery.application.service.DriverService;
import com.klp.delivery.delivery.application.service.IdempotencyKeyService;
import com.klp.delivery.delivery.application.util.DriverSelector;
import com.klp.delivery.delivery.domain.entity.Delivery;
import com.klp.delivery.delivery.domain.entity.DeliveryItem;
import com.klp.delivery.delivery.domain.event.DeliveryRouteCreateEvent;
import com.klp.delivery.delivery.domain.event.OrderDeliveryEvent;
import com.klp.delivery.delivery.presentation.dto.DeliveryResponse;
import com.klp.delivery.global.exception.BusinessException;
import com.klp.delivery.routeplan.application.command.HubInfo;
import com.klp.delivery.routeplan.application.service.HubClientService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryFacade {

    private final DeliveryService deliveryService;
    private final IdempotencyKeyService idempotencyKeyService;
    private final ApplicationEventPublisher eventPublisher;
    private final DriverService driverService;
    private final HubClientService hubService;
    private final DeliveryEventPublisher deliveryEventPublisher;


    @Transactional
    public DeliveryResponse createDelivery(OrderToDeliveryCommand orderCommand,
        IdempotencyCommand idempotencyCommand) {
        log.info("배송 생성 시작: orderId={}", orderCommand.orderId());

        // 멱등키 등록 (주문 단위로 관리)
        idempotencyKeyService.registerIdempotencyKey(idempotencyCommand);

        try {

            HubInfo arrivalHubInfo = hubService.getHubById(orderCommand.userAddressHubId());

            // 업체 배송 담당자 조회
            List<DriverCommand> driverList = driverService.findArrivalHubDrivers(
                UUID.fromString(arrivalHubInfo.hubId().toString()));

            // 업체 배송 담당자 지정
            DriverCommand driverCommand = DriverSelector.pickRandomDriver(driverList);

            // 항목별 배송 생성
            List<DeliveryResponse.DeliveryItemResponse> deliveryItems = createDeliveriesForOrderItems(
                orderCommand,
                arrivalHubInfo, driverCommand);

            IdempotencyCommand updateCommand = new IdempotencyCommand(
                idempotencyCommand.idempotencyKey(), idempotencyCommand.orderId(),
                IdempotencyStatus.COMPLETED);

            idempotencyKeyService.updateIdempotencyStatus(updateCommand);

            log.info("배송 생성 완료: orderId={}, deliveryCount={}", orderCommand.orderId(),
                deliveryItems.size());
            return new DeliveryResponse(orderCommand.orderId(), deliveryItems);
        } catch (Exception e) {
            log.error("배송 생성 실패: orderId={}, error={}", orderCommand.orderId(), e.getMessage(), e);
            try {
                // 실패 시 멱등키 삭제하여 재시도 가능하도록 처리
                idempotencyKeyService.deleteIdempotencyKey(idempotencyCommand.idempotencyKey());
                log.info("배송 생성 실패로 인한 멱등키 삭제 완료: idempotencyKey={}", idempotencyCommand.idempotencyKey());
            } catch (Exception deleteException) {
                log.error("배송 생성 실패로 인한  멱등키 삭제 실패: idempotencyKey={}, error={}",
                    idempotencyCommand.idempotencyKey(), deleteException.getMessage(), deleteException);
            }
            throw new BusinessException(DELIVERY_CREATION_FAILED);
        }
    }

    private List<DeliveryResponse.DeliveryItemResponse> createDeliveriesForOrderItems(
        OrderToDeliveryCommand orderCommand, HubInfo arrivalHubInfo,
        DriverCommand driverCommand) {

        // 1. 출발지 허브별로 주문 아이템 그룹화
        Map<UUID, List<OrderItemCommand>> itemsByHub = orderCommand.items().stream()
            .collect(Collectors.groupingBy(OrderItemCommand::hubId));

        // 2. 각 출발지 허브마다 배송 생성 및 경로 생성 이벤트 발행
        List<Delivery> createdDeliveries = new ArrayList<>();
        UUID arrivalHubId = arrivalHubInfo.hubId();

        for (Map.Entry<UUID, List<OrderItemCommand>> entry : itemsByHub.entrySet()) {
            UUID departureHubId = entry.getKey();
            List<OrderItemCommand> orderItems = entry.getValue();

            HubInfo departureHub = hubService.getHubById(departureHubId);
            DeliveryCommand deliveryCommand = createDeliveryCommand(
                orderCommand, departureHub, arrivalHubId, driverCommand);

            Delivery delivery = deliveryService.registerDelivery(deliveryCommand, orderItems);
            createdDeliveries.add(delivery);

            // 각 배송 생성 직후 경로 생성 이벤트 발행 (비동기)
            publishDeliveryRouteCreateEvent(delivery);
        }

        // 3. Response 생성
        List<DeliveryResponse.DeliveryItemResponse> deliveryItems = buildDeliveryItemResponses(
            createdDeliveries);

        // 4. 이벤트 데이터 수집
        List<OrderDeliveryEvent.DeliveryItem> eventItems = buildEventItems(createdDeliveries);

        // 5. 배송 생성 이벤트 발행
        publishDeliveryCreatedEvent(orderCommand.orderId(), eventItems);

        return deliveryItems;
    }

    private DeliveryCommand createDeliveryCommand(
        OrderToDeliveryCommand orderCommand, HubInfo departureHub,
        UUID arrivalHubId, DriverCommand driverCommand) {

        return new DeliveryCommand(
            orderCommand.orderId(),
            departureHub.hubId(),
            departureHub.name(),
            arrivalHubId,
            departureHub.name(),
            orderCommand.name(),
            orderCommand.address(),
            driverCommand.slackId(),
            driverCommand.userId()
        );
    }

    private List<DeliveryResponse.DeliveryItemResponse> buildDeliveryItemResponses(
        List<Delivery> deliveries) {

        List<DeliveryResponse.DeliveryItemResponse> responses = new ArrayList<>();
        for (Delivery delivery : deliveries) {
            for (DeliveryItem item : delivery.getDeliveryItems()) {
                responses.add(new DeliveryResponse.DeliveryItemResponse(
                    item.getOrderItemId(),
                    delivery.getDeliveryId()
                ));
            }
        }
        return responses;
    }

    private List<OrderDeliveryEvent.DeliveryItem> buildEventItems(List<Delivery> deliveries) {
        List<OrderDeliveryEvent.DeliveryItem> eventItems = new ArrayList<>();
        for (Delivery delivery : deliveries) {
            for (DeliveryItem item : delivery.getDeliveryItems()) {
                eventItems.add(new OrderDeliveryEvent.DeliveryItem(
                    item.getOrderItemId(),
                    delivery.getDeliveryId()
                ));
            }
        }
        return eventItems;
    }

    private void publishDeliveryCreatedEvent(
        UUID orderId,List<OrderDeliveryEvent.DeliveryItem> eventItems) {

        if (eventItems.isEmpty()) {
            return;
        }

        OrderDeliveryEvent event = new OrderDeliveryEvent(
            orderId,
            CustomerDeliveryStatus.CREATED.name(),
            eventItems
        );

        deliveryEventPublisher.publishCreatedEvent(event);
        log.info("배송 생성 이벤트 발행: orderId={}, status={}, totalItems={}",
            orderId, CustomerDeliveryStatus.CREATED, eventItems.size());
    }

    private void publishDeliveryRouteCreateEvent(Delivery delivery) {
        eventPublisher.publishEvent(
            new DeliveryRouteCreateEvent(
                delivery.getDeliveryId(),
                delivery.getDepartureId(),
                delivery.getDepartureName(),
                delivery.getArrivalId(),
                delivery.getArrivalName(),
                delivery.getUserDrvierId()
            )
        );
        log.info("배송 경로 생성 이벤트 발행: deliveryId={}, orderId={}",
            delivery.getDeliveryId(), delivery.getOrderId());
    }

    @Transactional
    public void updateVendorDriver(UUID deliveryId, Long vendorDrvierId) {

        Delivery delivery = deliveryService.findDelivery(deliveryId);
        driverService.findDriverAtArrivalHub(vendorDrvierId);

        delivery.updateUserDriverId(vendorDrvierId);

    }

    public boolean hasActiveDeliveries(UUID routePlanId) {
        return deliveryService.hasActiveDeliveriesByRoutePlanId(routePlanId);
    }
}