package com.klp.delivery.delivery.application.service;

import static com.klp.delivery.routeplan.exception.RoutePlanErrorCode.NO_ROUTE_PLAN_FOUND;

import com.klp.common.exception.BusinessException;
import com.klp.delivery.common.enums.CustomerDeliveryStatus;
import com.klp.delivery.common.enums.DeliveryRouteStatus;
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
import com.klp.delivery.routeplan.presentation.dto.response.GetRoutePlanDetailResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            DeliveryRouteStatus status;

            // 경로아이템 size조회
            if (planCommand.planItems().isEmpty()) {
                log.info(
                    "중간 허브 없음 - 직행 경로: deliveryId={}, departureId={}, arrivalId={}, totalDistance={}km, totalDuration={}min",
                    deliveryCommand.deliveryId(), planCommand.departureId(),
                    planCommand.arrivalId(),
                    planCommand.totalDistanceKm(), planCommand.totalDurationMin());

                // 중간 허브 없음 → 바로 최종 허브 도착
                status = DeliveryRouteStatus.ARRIVED_AT_FINAL_HUB;
                route = DeliveryRoute.create(
                    deliveryCommand.deliveryId(),
                    deliveryCommand.driverId(),
                    deliveryCommand.departureId(),
                    deliveryCommand.departureName(),
                    deliveryCommand.arrivalId(),
                    deliveryCommand.arrivalName(),
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
                status = DeliveryRouteStatus.IN_HUB_TRANSIT;

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
                    plan.departureName(),
                    plan.arrivalId(),
                    plan.arrivalName(),
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
                create.getDepartureId(), create.getArrivalId());

            CustomerDeliveryStatus deliveryStatus = convertToCustomerDeliveryStatus(status);

            return new DeliveryRouteStatusCommand(create.getDeliveryRouteId(), deliveryStatus);

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
    CustomerDeliveryStatus convertToCustomerDeliveryStatus(
        DeliveryRouteStatus deliveryRouteStatus) {
        return switch (deliveryRouteStatus) {
            case CREATED -> CustomerDeliveryStatus.CREATED;
            case IN_HUB_TRANSIT, AT_INTERMEDIATE_HUB, ARRIVED_AT_FINAL_HUB, OUT_FOR_DELIVERY ->
                CustomerDeliveryStatus.SHIPPING;
            case DELIVERED -> CustomerDeliveryStatus.ARRIVED;
        };
    }

    @Transactional
    public DeliveryRouteStatusCommand appendDeliveryRoute(UUID deliveryId,
        GetRoutePlanDetailResponse routePlan, DeliveryRouteStatus currentDeliveryRouteStatus,
        Long vendorDriverId) {
        log.info("배송 경로 추가 시작: deliveryId={}, routePlanId={}, currentStatus={}, vendorDriverId={}",
            deliveryId, routePlan.routePlanId(), currentDeliveryRouteStatus, vendorDriverId);

        try {
            // 현재 상태가 배송완료(DELIVERED)면 예외 발생
            if (currentDeliveryRouteStatus == DeliveryRouteStatus.DELIVERED) {
                throw new BusinessException(DeliveryErrorCode.DELIVERY_CANNOT_BE_MODIFIED,
                    "이미 배송이 완료된 상태입니다. 배송 경로 기록을 추가할 수 없습니다.");
            }

            // 현재 배송 경로 목록 조회
            List<DeliveryRoute> existingRoutes = deliveryRouteRepository.findByDeliveryId(
                deliveryId);
            if (existingRoutes.isEmpty()) {
                throw new BusinessException(DeliveryErrorCode.DELIVERY_ROUTE_FETCH_FAILED);
            }

            // 현재 배송 경로 마지막 경로 찾기
            DeliveryRoute currentLastRoute = existingRoutes.stream()
                .max(java.util.Comparator.comparing(DeliveryRoute::getSequence))
                .orElseThrow(
                    () -> new BusinessException(DeliveryErrorCode.DELIVERY_ROUTE_FETCH_FAILED,
                        "마지막 배송 경로를 찾을 수 없습니다."));

            // 다음 sequence의 PlanItem 찾기
            int nextSequence = currentLastRoute.getSequence() + 1;
            GetRoutePlanDetailResponse.PlanItem nextPlanItem = routePlan.planItems().stream()
                .filter(item -> item.sequence().equals(nextSequence))
                .findFirst()
                .orElseThrow(
                    () -> new BusinessException(DeliveryErrorCode.DELIVERY_ROUTE_FETCH_FAILED,
                        "다음 경로 계획을 찾을 수 없습니다. sequence=" + nextSequence));

            // 마지막 sequence 확인
            int lastSequence = routePlan.planItems().stream()
                .map(GetRoutePlanDetailResponse.PlanItem::sequence)
                .max(Integer::compareTo)
                .orElse(1);

            DeliveryRouteStatus routeStatus = determineRouteStatus(
                nextSequence, lastSequence, nextPlanItem, routePlan.arrivalId(),
                currentLastRoute.getArrivalId(), currentDeliveryRouteStatus);

            CustomerDeliveryStatus deliveryStatus = determineDeliveryStatus(
                currentDeliveryRouteStatus,
                nextPlanItem, routePlan.arrivalId(), lastSequence);

            Long driverId = selectDriverId(currentDeliveryRouteStatus, routeStatus, vendorDriverId);

            DeliveryRoute newRoute = DeliveryRoute.create(
                deliveryId,
                driverId,
                currentLastRoute.getDepartureId(),
                currentLastRoute.getDepartureName(),
                nextPlanItem.arrivalId(),
                nextPlanItem.arrivalName(),
                nextPlanItem.sequence(),
                nextPlanItem.distanceKm(),
                nextPlanItem.durationMin(),
                nextPlanItem.distanceKm(),
                nextPlanItem.durationMin(),
                routeStatus
            );

            DeliveryRoute savedRoute = deliveryRouteRepository.save(newRoute);
            log.info(
                "배송 경로 추가 완료: deliveryId={}, routeId={}, sequence={}, routeStatus={}, deliveryStatus={}, driverId={}, departureId={}, arrivalId={}",
                deliveryId, savedRoute.getDeliveryRouteId(), nextSequence, routeStatus,
                deliveryStatus, driverId, currentLastRoute.getDepartureId(), nextPlanItem.arrivalName());

            return new DeliveryRouteStatusCommand(savedRoute.getDeliveryRouteId(),
                deliveryStatus);
        } catch (Exception e) {
            log.error("배송 경로 추가 실패: {}", e.getMessage(), e);
            throw new BusinessException(DeliveryErrorCode.DELIVERY_ROUTE_CREATION_FAILED);
        }
    }


    private Long selectDriverId(DeliveryRouteStatus currentStatus, DeliveryRouteStatus newStatus,
        Long vendorDriverId) {

        // 배송출발(OUT_FOR_DELIVERY) 또는 배송완료(DELIVERED)에서는 업체 배송 담당자 선택
        if (currentStatus == DeliveryRouteStatus.ARRIVED_AT_FINAL_HUB
            || currentStatus == DeliveryRouteStatus.OUT_FOR_DELIVERY
            || newStatus == DeliveryRouteStatus.OUT_FOR_DELIVERY
            || newStatus == DeliveryRouteStatus.DELIVERED) {
            log.info("업체 배송 담당자 사용: vendorDriverId={}, currentStatus={}, newStatus={}",
                vendorDriverId, currentStatus, newStatus);
            return vendorDriverId;
        }

        // 그 외에는 물류 배송 담당자 선택
        List<DriverResponse> driverList = driverClientService.findLogisticsDrivers();
        DriverCommand driver = DriverSelector.pickRandomDriver(DriverCommand.from(driverList));
        log.info("물류 배송 담당자 선택: driverId={}, currentStatus={}, newStatus={}",
            driver.userId(), currentStatus, newStatus);
        return driver.userId();
    }


    /**
     * Route 상태 1. 추가되는 route의 sequence가 마지막 시퀀스라면 → ARRIVED_AT_FINAL_HUB (최종 허브 도착) 2. 현재 배송경로의
     * 도착허브가 최종 허브가 아니며 현재 상태가 IN_HUB_TRANSIT(허브 간 이동 중)인 경우 → AT_INTERMEDIATE_HUB (중간 허브 도착) 3. 허브
     * 도착 이후 다음 허브로 이동할 때 → IN_HUB_TRANSIT (허브 간 이동 중)
     */
    private DeliveryRouteStatus determineRouteStatus(int nextSequence, int lastSequence,
        GetRoutePlanDetailResponse.PlanItem nextPlanItem, UUID finalArrivalHubId,
        UUID currentArrivalHubId, DeliveryRouteStatus currentDeliveryRouteStatus) {

        // 1) sequence가 마지막 시퀀스라면 → ARRIVED_AT_FINAL_HUB (최종 허브 도착)
        if (nextSequence == lastSequence) {
            log.info("최종 허브 도착 route: sequence={}, arrivalHubId={}", nextSequence,
                nextPlanItem.arrivalId());
            return DeliveryRouteStatus.ARRIVED_AT_FINAL_HUB;
        }

        // 2) 현재 배송경로의 arrivalHubId가 최종 허브가 아니며 현재 상태가 IN_HUB_TRANSIT인 경우 → AT_INTERMEDIATE_HUB
        if (!currentArrivalHubId.equals(finalArrivalHubId)
            && currentDeliveryRouteStatus == DeliveryRouteStatus.IN_HUB_TRANSIT) {
            log.info("중간 허브 도착 route: sequence={}, arrivalHubId={}", nextSequence,
                nextPlanItem.arrivalId());
            return DeliveryRouteStatus.AT_INTERMEDIATE_HUB;
        }

        // 3) 허브 도착 이후 다음 허브로 이동할 때 → IN_HUB_TRANSIT (허브 간 이동 중)
        log.info("허브 간 이동 중 route: sequence={}, departureId={}, arrivalId={}",
            nextSequence, nextPlanItem.departureId(), nextPlanItem.arrivalId());
        return DeliveryRouteStatus.IN_HUB_TRANSIT;
    }



    private CustomerDeliveryStatus determineDeliveryStatus(DeliveryRouteStatus currentStatus,
        GetRoutePlanDetailResponse.PlanItem nextPlanItem, UUID finalArrivalHubId,
        int lastSequence) {

        if (currentStatus == DeliveryRouteStatus.ARRIVED_AT_FINAL_HUB) {
            log.info("최종 허브 도착 후 배송 출발: ARRIVED_AT_FINAL_HUB → OUT_FOR_DELIVERY → SHIPPING");
            return CustomerDeliveryStatus.SHIPPING;
        }

        if (currentStatus == DeliveryRouteStatus.OUT_FOR_DELIVERY) {
            log.info("배송 완료: OUT_FOR_DELIVERY → DELIVERED → ARRIVED");
            return CustomerDeliveryStatus.ARRIVED;
        }

        int nextSequence = nextPlanItem.sequence();
        if (nextPlanItem.arrivalId().equals(finalArrivalHubId) && nextSequence == lastSequence) {
            log.info("최종 허브 도착: Delivery 상태 → ARRIVED_AT_FINAL_HUB → SHIPPING");
            return CustomerDeliveryStatus.SHIPPING;
        }

        log.info("배송 중: Delivery 상태 → SHIPPING (중간 허브 도착 또는 이동 중)");
        return CustomerDeliveryStatus.SHIPPING;
    }

    @Transactional(readOnly = true)
    public List<DeliveryRoute> findByDeliveryId(UUID deliveryId) {
        return deliveryRouteRepository.findByDeliveryId(deliveryId);
    }
}
