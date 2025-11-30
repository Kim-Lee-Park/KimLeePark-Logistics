package com.klp.delivery.delivery.application.service;

import static com.klp.delivery.routeplan.exception.RoutePlanErrorCode.NO_ROUTE_PLAN_FOUND;

import com.klp.common.exception.BusinessException;
import com.klp.delivery.common.enums.CustomerDeliveryStatus;
import com.klp.delivery.common.enums.DeliveryStatus;
import com.klp.delivery.delivery.application.command.DeliveryRouteCommand;
import com.klp.delivery.delivery.application.command.DeliveryRoutePlanCommand;
import com.klp.delivery.delivery.application.command.DeliveryRoutePlanCommand.PlanItem;
import com.klp.delivery.delivery.application.command.DeliveryRouteStatusCommand;
import com.klp.delivery.delivery.application.command.DriverCommand;
import com.klp.delivery.delivery.application.util.DriverSelector;
import com.klp.delivery.delivery.domain.entity.DeliveryRoute;
import com.klp.delivery.delivery.domain.repository.DeliveryRouteRepository;
import com.klp.delivery.delivery.exception.DeliveryErrorCode;
import com.klp.delivery.delivery.infrastructure.client.dto.DriverResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryRouteService {

    private final DeliveryRouteRepository deliveryRouteRepository;
    private final DriverClientService driverClientService;

    public DeliveryRouteStatusCommand createDeliveryRoute(DeliveryRouteCommand deliveryCommand,
        DeliveryRoutePlanCommand planCommand) {
        log.info("배송 경로 생성 시작: deliveryId={}, routePlanId={}, departureId={}, arrivalId={}",
            deliveryCommand.deliveryId(), planCommand.routePlanId(), planCommand.departureId(),
            planCommand.arrivalId());

        try {
            DeliveryRoute route;
            DeliveryStatus status;

            // 경로아이템 size조회
            if (planCommand.planItems().isEmpty()) {
                log.info(
                    "중간 허브 없음 - 직행 경로: deliveryId={}, departureId={}, arrivalId={}, totalDistance={}km, totalDuration={}min",
                    deliveryCommand.deliveryId(), planCommand.departureId(),
                    planCommand.arrivalId(),
                    planCommand.totalDistanceKm(), planCommand.totalDurationMin());

                // 중간 허브 없음 → 바로 최종 허브 도착
                status = DeliveryStatus.ARRIVED_AT_FINAL_HUB;
                route = DeliveryRoute.create(
                    deliveryCommand.deliveryId(),
                    deliveryCommand.vendorDrvierId(),
                    planCommand.departureId(),
                    planCommand.arrivalId(),
                    1,  // 첫 등록이므로 0부터 시작
                    planCommand.totalDistanceKm(),
                    planCommand.totalDurationMin(),
                    planCommand.totalDistanceKm(),
                    planCommand.totalDurationMin(),
                    status // 중간허브가 없으므로 마지막 허브
                );
            } else {
                log.info(
                    "중간 허브 있음 - 경유 경로: deliveryId={}, planItemCount={}, departureId={}, arrivalId={}",
                    deliveryCommand.deliveryId(), planCommand.planItems().size(),
                    planCommand.departureId(), planCommand.arrivalId());

                // 중간 허브가 있음 허브로 이동 중
                status = DeliveryStatus.IN_HUB_TRANSIT;

                // 경로계획 중 첫번째
                PlanItem plan =
                    planCommand.planItems().stream()
                        .findFirst()
                        .orElseThrow(() -> new BusinessException(NO_ROUTE_PLAN_FOUND));

                // 물류배송담당자 조회
                List<DriverResponse> driverList = driverClientService.findLogisticsDrivers();

                DriverCommand driver = DriverSelector.pickRandomDriver(
                    DriverCommand.from(driverList));

                route = DeliveryRoute.create(
                    deliveryCommand.deliveryId(),
                    driver.userId(),
                    plan.departureId(),
                    plan.arrivalId(),
                    plan.sequence(),
                    plan.distanceKm(),
                    plan.durationMin(),
                    plan.distanceKm(),
                    plan.durationMin(),
                    status
                );
            }

            DeliveryRoute create = deliveryRouteRepository.save(route);
            log.info(
                "배송 경로 생성 완료: deliveryId={}, routeId={}, routeStatus={}, departureId={}, arrivalId={}",
                deliveryCommand.deliveryId(), create.getDeliveryRouteId(), status,
                create.getDepartureHubId(), create.getArrivalHubId());

            CustomerDeliveryStatus deliveryStatus = convertToCustomerDeliveryStatus(status);

            return new DeliveryRouteStatusCommand(create.getDeliveryRouteId(), deliveryStatus);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("배송 경로 생성 실패: {}", e.getMessage(), e);
            throw new BusinessException(DeliveryErrorCode.DELIVERY_ROUTE_CREATION_FAILED);
        }
    }

    /**
     * 고객 노출 상태 매핑
     * CREATED → CREATED
     * (IN_HUB_TRANSIT, AT_INTERMEDIATE_HUB, ARRIVED_AT_FINAL_HUB, OUT_FOR_DELIVERY) → SHIPPING
     * DELIVERED → ARRIVED
     */
    CustomerDeliveryStatus convertToCustomerDeliveryStatus(DeliveryStatus deliveryStatus) {
        return switch (deliveryStatus) {
            case CREATED -> CustomerDeliveryStatus.CREATED;
            case IN_HUB_TRANSIT, AT_INTERMEDIATE_HUB, ARRIVED_AT_FINAL_HUB, OUT_FOR_DELIVERY ->
                CustomerDeliveryStatus.SHIPPING;
            case DELIVERED -> CustomerDeliveryStatus.ARRIVED;
        };
    }
}
