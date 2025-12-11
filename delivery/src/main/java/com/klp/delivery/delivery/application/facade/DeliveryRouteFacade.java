package com.klp.delivery.delivery.application.facade;

import com.klp.delivery.common.enums.DeliveryRouteStatus;
import com.klp.delivery.delivery.application.command.DeliveryRouteCommand;
import com.klp.delivery.delivery.application.command.DeliveryRoutePlanCommand;
import com.klp.delivery.common.enums.CustomerDeliveryStatus;
import com.klp.delivery.delivery.application.command.DeliveryRouteStatusCommand;
import com.klp.delivery.delivery.application.command.OrderToDeliveryCommand.OrderItemCommand;
import com.klp.delivery.delivery.application.event.DeliveryEventPublisher;
import com.klp.delivery.delivery.application.service.DeliveryOutboxEventService;
import com.klp.delivery.delivery.application.service.DeliveryRouteService;
import com.klp.delivery.delivery.application.service.DeliveryService;
import com.klp.delivery.delivery.domain.entity.Delivery;
import com.klp.delivery.delivery.domain.event.DeliveryNotificationEvent;
import com.klp.delivery.delivery.domain.event.DeliveryRouteCreateEvent;
import com.klp.delivery.delivery.domain.event.OrderDeliveryEvent;
import com.klp.delivery.delivery.domain.entity.DeliveryRoute;
import com.klp.delivery.delivery.domain.repository.DeliveryRouteRepository;
import com.klp.delivery.delivery.presentation.dto.DeliveryRouteResponse;
import com.klp.delivery.routeplan.application.service.RoutePlanService;
import com.klp.delivery.routeplan.presentation.dto.response.GetRoutePlanDetailResponse;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryRouteFacade {

    private final DeliveryRouteService deliveryRouteService;
    private final DeliveryService deliveryService;
    private final RoutePlanService routePlanService;
    private final DeliveryRouteRepository deliveryRouteRepository;
    private final DeliveryEventPublisher deliveryEventPublisher;
    private final DeliveryOutboxEventService deliveryOutboxEventService;

    @Async("DeliveryRouteExecutor")
    @Transactional
    public void CreateDeliveryRoute(DeliveryRouteCommand command,
        DeliveryRouteCreateEvent routeCreateEvent) {

        // 허브 경로 계획 조회
        GetRoutePlanDetailResponse response = routePlanService.getRoutePlan(command.departureId(),
            command.arrivalId());

        DeliveryRoutePlanCommand routePlancommand = DeliveryRoutePlanCommand.toDeliveryRoutePlanCommand(
            response);

        // 첫 번째 경로만 생성 (나머지 경로는 수동 API 호출로 생성)
        DeliveryRouteStatusCommand statusCommand = deliveryRouteService.createDeliveryRoute(command,
            routePlancommand);

        // 배송업데이트 (routePlanId와 status 업데이트)
        deliveryService.applyRouteCreation(command.deliveryId(), routePlancommand.routePlanId(),
            statusCommand.status());

        // 배송 경로 생성 완료
        Delivery delivery = deliveryService.findDelivery(command.deliveryId());
        log.info("배송 경로 생성 완료: deliveryId={}, orderId={}, status={}",
            command.deliveryId(), delivery.getOrderId(), delivery.getStatus());

        // 첫 경로 생성 시 CREATED -> SHIPPING으로 변경될 때만 DeliveryShippingEvent 아웃박스 저장
        if (statusCommand.status() == CustomerDeliveryStatus.SHIPPING) {
            List<DeliveryShippingEvent.DeliveryItem> eventItems = delivery.getDeliveryItems().stream()
                .map(item -> new DeliveryShippingEvent.DeliveryItem(
                    item.getOrderItemId(),
                    delivery.getDeliveryId()
                ))
                .toList();

            DeliveryShippingEvent shippingEvent = new DeliveryShippingEvent(
                delivery.getOrderId(),
                statusCommand.status().name(),
                eventItems
            );
            deliveryOutboxEventService.saveShippingEvent(
                delivery.getDeliveryId(),
                delivery.getOrderId(),
                shippingEvent
            );
            log.info("배송 중 이벤트 아웃박스 저장: deliveryId={}, orderId={}, status={}, items={}",
                command.deliveryId(), delivery.getOrderId(), statusCommand.status().name(), eventItems.size());
        }

        // 배송 경로 생성 완료 후 Notification 이벤트 발행 (이미 가지고 있는 정보 활용)
        publishNotificationEvent(delivery, response, statusCommand, routeCreateEvent);
    }

    private void publishNotificationEvent(
        Delivery delivery,
        GetRoutePlanDetailResponse routePlan,
        DeliveryRouteStatusCommand statusCommand,
        DeliveryRouteCreateEvent routeCreateEvent) {
        try {
            // 1. 첫 번째 배송 아이템의 상품 정보 사용
            OrderItemCommand firstOrderItem =
                routeCreateEvent.orderItems().stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("주문 아이템을 찾을 수 없습니다."));

            // 2. 경유 허브 이름 목록 추출 (출발지와 도착지 제외)
            List<String> transitHubNames = routePlan.planItems().stream()
                .filter(item -> item.sequence() > 1 && item.sequence() < routePlan.planItems().size())
                .map(GetRoutePlanDetailResponse.PlanItem::arrivalName)
                .collect(Collectors.toList());

            DeliveryNotificationEvent notificationEvent = new DeliveryNotificationEvent(
                delivery.getDeliveryId(),
                routeCreateEvent.orderId(),
                delivery.getUserDriverSlackId(),
                routeCreateEvent.ordererName(),
                routeCreateEvent.ordererEmail(),
                routeCreateEvent.orderTime(),
                firstOrderItem.productName(),
                firstOrderItem.quantity(),
                routeCreateEvent.requirements(),
                delivery.getDepartureName(),
                transitHubNames,
                delivery.getUserAddress(),
                routeCreateEvent.driverName(),
                routeCreateEvent.driverEmail(),
                "", // TODO: workingHours 추후 추가 예정
                routeCreateEvent.orderTime() // occurredAt은 orderCreateAt과 동일
            );

            // 아웃박스 패턴으로 이벤트 저장
            deliveryOutboxEventService.saveEvent(
                delivery.getDeliveryId(),
                routeCreateEvent.orderId(),
                "DELIVERY_NOTIFICATION",
                notificationEvent
            );
            log.info("배송 알림 이벤트 아웃박스 저장 완료: deliveryId={}, orderId={}, departureHubName={}",
                delivery.getDeliveryId(), delivery.getOrderId(), delivery.getDepartureName());

        } catch (Exception e) {
            log.error("배송 알림 이벤트 발행 실패: deliveryId={}, orderId={}",
                delivery.getDeliveryId(), delivery.getOrderId(), e);
            // Notification 이벤트 발행 실패는 배송 경로 생성에 영향을 주지 않도록 예외를 던지지 않음
        }
    }

    @Transactional
    public DeliveryRouteResponse appendDeliveryRoute(UUID deliveryId) {
        log.info("배송 경로 추가 시작: deliveryId={}", deliveryId);

        // 배송 조회
        Delivery delivery = deliveryService.findDelivery(deliveryId);

        // 현재 배송 경로 목록 조회하여 마지막 경로의 상태 확인
        List<DeliveryRoute> existingRoutes = deliveryRouteRepository.findByDeliveryId(deliveryId);
        DeliveryRouteStatus currentRouteStatus = existingRoutes.stream()
            .max(Comparator.comparing(DeliveryRoute::getSequence))
            .map(DeliveryRoute::getStatus)
            .orElse(DeliveryRouteStatus.CREATED); // 첫 경로인 경우 CREATED

        // 경로 계획 조회
        GetRoutePlanDetailResponse routePlan = routePlanService.getRoutePlan(
            delivery.getDepartureId(),
            delivery.getArrivalId()
        );

        // 다음 경로 추가 및 Delivery 상태 결정
        DeliveryRouteStatusCommand statusCommand = deliveryRouteService.appendDeliveryRoute(
            deliveryId,
            routePlan,
            currentRouteStatus,
            delivery.getUserDrvierId()
        );

        // Delivery 상태 업데이트
        deliveryService.applyRouteCreation(deliveryId, routePlan.routePlanId(),
            statusCommand.status());

        // 업데이트된 배송 조회
        Delivery updatedDelivery = deliveryService.findDelivery(deliveryId);
        log.info("배송 경로 추가 완료: deliveryId={}, routeId={}, status={}",
            deliveryId, statusCommand.deliveryRouteId(), statusCommand.status());

        // 배송 완료(ARRIVED)일 때만 OrderDeliveryEvent 발행
        if (statusCommand.status() == CustomerDeliveryStatus.ARRIVED) {
            List<DeliveryArrivedEvent.DeliveryItem> eventItems = updatedDelivery.getDeliveryItems().stream()
                .map(item -> new DeliveryArrivedEvent.DeliveryItem(
                    item.getOrderItemId(),
                    updatedDelivery.getDeliveryId()
                ))
                .toList();

            DeliveryArrivedEvent arrivedEvent = new DeliveryArrivedEvent(
                updatedDelivery.getOrderId(),
                statusCommand.status().name(),
                eventItems
            );
            deliveryOutboxEventService.saveArrivedEvent(
                updatedDelivery.getDeliveryId(),
                updatedDelivery.getOrderId(),
                arrivedEvent
            );
            log.info("배송 완료 이벤트 아웃박스 저장: deliveryId={}, orderId={}, status={}, items={}",
                deliveryId, updatedDelivery.getOrderId(), statusCommand.status().name(), eventItems.size());
        }

        return new DeliveryRouteResponse(deliveryId, statusCommand.deliveryRouteId());
    }
}
