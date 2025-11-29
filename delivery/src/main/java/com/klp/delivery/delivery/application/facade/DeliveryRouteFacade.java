package com.klp.delivery.delivery.application.facade;

import com.klp.delivery.delivery.application.command.DeliveryRouteCommand;
import com.klp.delivery.delivery.application.command.DeliveryRoutePlanCommand;
import com.klp.delivery.delivery.application.command.DeliveryRouteStatusCommand;
import com.klp.delivery.delivery.application.service.DeliveryRouteService;
import com.klp.delivery.delivery.application.service.DeliveryService;
import com.klp.delivery.routeplan.application.service.RoutePlanService;
import com.klp.delivery.routeplan.presentation.dto.response.GetRoutePlanDetailResponse;
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

}
