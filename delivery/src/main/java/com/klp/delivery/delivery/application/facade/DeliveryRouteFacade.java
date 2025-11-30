package com.klp.delivery.delivery.application.facade;

import com.klp.delivery.common.enums.DeliveryStatus;
import com.klp.delivery.delivery.application.command.DeliveryRouteCommand;
import com.klp.delivery.delivery.application.command.DeliveryRoutePlanCommand;
import com.klp.delivery.delivery.application.command.DeliveryRouteStatusCommand;
import com.klp.delivery.delivery.application.service.DeliveryRouteService;
import com.klp.delivery.delivery.application.service.DeliveryService;
import com.klp.delivery.delivery.application.service.DriverService;
import com.klp.delivery.delivery.domain.entity.Delivery;
import com.klp.delivery.delivery.domain.entity.DeliveryRoute;
import com.klp.delivery.delivery.domain.repository.DeliveryRouteRepository;
import com.klp.delivery.delivery.presentation.dto.DeliveryRouteResponse;
import com.klp.delivery.routeplan.application.service.RoutePlanService;
import com.klp.delivery.routeplan.presentation.dto.response.GetRoutePlanDetailResponse;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
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

    @Async("DeliveryRouteExecutor")
    @Transactional
    public void CreateDeliveryRoute(DeliveryRouteCommand command) {

        // 허브 경로 계획 조회
        GetRoutePlanDetailResponse response = routePlanService.getRoutePlan(command.departureId(),
            command.arrivalId());

        DeliveryRoutePlanCommand routePlancommand = DeliveryRoutePlanCommand.toDeliveryRoutePlanCommand(
            response);

        // 경로저장
        DeliveryRouteStatusCommand statusCommand = deliveryRouteService.createDeliveryRoute(command,
            routePlancommand);

        // 배송업데이트 (routePlanId와 status 업데이트)
        deliveryService.applyRouteCreation(command.deliveryId(), routePlancommand.routePlanId(),
            statusCommand.status());


    }

    @Transactional
    public DeliveryRouteResponse appendDeliveryRoute(UUID deliveryId) {
        log.info("배송 경로 추가 시작: deliveryId={}", deliveryId);

        // 배송 조회
        Delivery delivery = deliveryService.findDelivery(deliveryId);

        // 현재 배송 경로 목록 조회하여 마지막 경로의 상태 확인
        List<DeliveryRoute> existingRoutes = deliveryRouteRepository.findByDeliveryId(deliveryId);
        DeliveryStatus currentRouteStatus = existingRoutes.stream()
            .max(Comparator.comparing(DeliveryRoute::getSequence))
            .map(DeliveryRoute::getStatus)
            .orElse(DeliveryStatus.CREATED); // 첫 경로인 경우 CREATED

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
            delivery.getVendorDrvierId()
        );

        // Delivery 상태 업데이트
        deliveryService.applyRouteCreation(deliveryId, routePlan.routePlanId(),
            statusCommand.status());

        log.info("배송 경로 추가 완료: deliveryId={}, routeId={}, status={}",
            deliveryId, statusCommand.deliveryRouteId(), statusCommand.status());

        return new DeliveryRouteResponse(deliveryId, statusCommand.deliveryRouteId());
    }
}
