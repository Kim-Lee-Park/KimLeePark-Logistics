package com.klp.delivery.delivery.application.facade;

import static com.klp.delivery.delivery.exception.DeliveryErrorCode.DELIVERY_CREATION_FAILED;

import com.klp.delivery.common.enums.IdempotencyStatus;
import com.klp.delivery.delivery.application.command.DeliveryCommand;
import com.klp.delivery.delivery.application.command.DriverCommand;
import com.klp.delivery.delivery.application.command.IdempotencyCommand;
import com.klp.delivery.delivery.application.command.OrderToDeliveryCommand;
import com.klp.delivery.delivery.application.command.OrderToDeliveryCommand.OrderItemCommand;
import com.klp.delivery.delivery.application.service.DeliveryService;
import com.klp.delivery.delivery.application.service.DeliveryOutboxEventService;
import com.klp.delivery.delivery.application.service.DriverService;
import com.klp.delivery.delivery.application.service.IdempotencyKeyService;
import com.klp.delivery.delivery.application.util.DriverSelector;
import com.klp.delivery.delivery.domain.entity.Delivery;
import com.klp.delivery.delivery.domain.entity.DeliveryItem;
import com.klp.delivery.delivery.domain.event.DeliveryRouteCreateEvent;
import com.klp.delivery.delivery.domain.event.DeliveryCreatedEvent;
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
    private final DeliveryOutboxEventService deliveryOutboxEventService;


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

            log.info("업체 배송 담당자 조회 ={}", driverList);
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
                log.info("배송 생성 실패로 인한 멱등키 삭제 완료: idempotencyKey={}",
                    idempotencyCommand.idempotencyKey());
            } catch (Exception deleteException) {
                log.error("배송 생성 실패로 인한  멱등키 삭제 실패: idempotencyKey={}, error={}",
                    idempotencyCommand.idempotencyKey(), deleteException.getMessage(),
                    deleteException);
            }
            throw new BusinessException(DELIVERY_CREATION_FAILED);
        }
    }

    private List<DeliveryResponse.DeliveryItemResponse> createDeliveriesForOrderItems(
        OrderToDeliveryCommand orderCommand, HubInfo arrivalHubInfo,
        DriverCommand driverCommand) {

        // 1. 출발지 허브별로 주문 아이템 그룹화
        Map<UUID, List<OrderItemCommand>> itemsByHub = orderCommand.products().stream()
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
            publishDeliveryRouteCreateEvent(delivery, orderCommand, driverCommand, orderItems);
        }

        // 3. Response 생성
        List<DeliveryResponse.DeliveryItemResponse> deliveryItems = buildDeliveryItemResponses(
            createdDeliveries);

        // 4. DeliveryCreatedEvent 아웃박스 저장
        if (!createdDeliveries.isEmpty()) {
            // OrderItemCommand를 orderItemId로 매핑
            Map<UUID, OrderItemCommand> productMap = orderCommand.products().stream()
                .collect(Collectors.toMap(OrderItemCommand::orderItemId, item -> item));

            // 모든 배송의 items를 합침
            List<DeliveryCreatedEvent.OrderItem> allEventItems = new ArrayList<>();
            for (Delivery delivery : createdDeliveries) {
                for (DeliveryItem item : delivery.getDeliveryItems()) {
                    allEventItems.add(new DeliveryCreatedEvent.OrderItem(
                        item.getOrderItemId(),
                        delivery.getDeliveryId()
                    ));
                }
            }
            // 첫 번째 배송의 정보 사용
            Delivery firstDelivery = createdDeliveries.get(0);
            DeliveryCreatedEvent createdEvent = DeliveryCreatedEvent.from(orderCommand, allEventItems);


            deliveryOutboxEventService.saveCreatedEvent(
                firstDelivery.getDeliveryId(),
                orderCommand.orderId(),
                createdEvent
            );

            log.info("배송 생성 이벤트 아웃박스 저장 완료: orderId={}, deliveryCount={}, totalItems={}",
                orderCommand.orderId(), createdDeliveries.size(), allEventItems.size());
        }

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
            orderCommand.username(),
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



    private void publishDeliveryRouteCreateEvent(
        Delivery delivery,
        OrderToDeliveryCommand orderCommand,
        DriverCommand driverCommand,
        List<OrderItemCommand> orderItems) {
        eventPublisher.publishEvent(
            new DeliveryRouteCreateEvent(
                delivery.getDeliveryId(),
                delivery.getOrderId(),
                delivery.getDepartureId(),
                delivery.getDepartureName(),
                delivery.getArrivalId(),
                delivery.getArrivalName(),
                delivery.getUserDrvierId(),
                delivery.getUserDriverSlackId(),
                delivery.getUserAddress(),
                orderCommand.username(),
                orderCommand.email(),
                orderCommand.createdAt(),
                orderCommand.comment() != null ? orderCommand.comment() : "",
                orderItems,
                driverCommand.username(),
                driverCommand.email()
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

    @Transactional
    public void cancelDeliveriesByOrderId(UUID orderId, Long deletedBy) {

        List<Delivery> deliveries = deliveryService.findDeliveriesByOrderIdForCancellation(orderId);

        if (deliveries.isEmpty()) {
            log.info("취소할 배송이 없습니다: orderId={}", orderId);
            return;
        }

        for (Delivery delivery : deliveries) {
            try {
                deliveryService.deleteDelivery(delivery.getDeliveryId(), deletedBy);
                log.info("배송 취소 완료: deliveryId={}, orderId={}", delivery.getDeliveryId(), orderId);
            } catch (Exception e) {
                log.error("배송 취소 실패: deliveryId={}, orderId={}, error={}",
                    delivery.getDeliveryId(), orderId, e.getMessage(), e);
            }
        }

        log.info("배송 취소 완료: orderId={}, cancelledCount={}", orderId, deliveries.size());
    }
}