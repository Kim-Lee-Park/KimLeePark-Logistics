package com.klp.delivery.routeplan.application.service;

import com.klp.common.exception.BusinessException;
import com.klp.delivery.routeplan.application.command.CreateRoutePlanCommand;
import com.klp.delivery.routeplan.application.command.HubInfo;
import com.klp.delivery.routeplan.application.command.HubRouteInfo;
import com.klp.delivery.routeplan.domain.model.RoutePlan;
import com.klp.delivery.routeplan.domain.policy.RoutePlanPolicy;
import com.klp.delivery.routeplan.domain.repository.RoutePlanRepository;
import com.klp.delivery.routeplan.exception.RoutePlanErrorCode;
import com.klp.delivery.routeplan.presentation.dto.response.CreateRoutePlanResponse;
import com.klp.delivery.routeplan.presentation.dto.response.GetRoutePlanDetailResponse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoutePlanService {
    private final RoutePlanRepository routePlanRepository;
    private final HubClientService hubClientService;
    private final HubRouteInfoClientService routeInfoClientService;
    private final RoutePlanPolicy routePlanPolicy;

    //경로 계획 생성
    @Transactional
    public CreateRoutePlanResponse createRoutePlan(CreateRoutePlanCommand command) {
        //중복 검증하기
        if(routePlanRepository.existsByDepartureIdAndArrivalId(command.departureId(),command.arrivalId())){
            log.warn(
                "[RoutePlanService] 경로 계획 중복 생성 시도 - departureId={}, arrivalId={}",
                command.departureId(),
                command.arrivalId()
            );
            throw new BusinessException(RoutePlanErrorCode.ALREADY_EXISTS_ROUTE_PLAN);
        }

        //출발 허브 조회
        HubInfo departureHub = hubClientService.getHubById(command.departureId());
        if(departureHub==null){
            log.warn("[RoutePlanService] 허브 조회 실패 - hubId: {} 존재하지 않음",
                command.departureId());
            throw new BusinessException(RoutePlanErrorCode.HUB_NOT_FOUND);
        }

        //도착 허브 조회
        HubInfo arrivalHub = hubClientService.getHubById(command.arrivalId());
        if(arrivalHub==null){
            log.warn("[RoutePlanService] 허브 조회 실패 - hubId: {} 존재하지 않음",
                command.arrivalId());
            throw new BusinessException(RoutePlanErrorCode.HUB_NOT_FOUND);
        }

        //출발-도착 허브간 이동 정보 조회
        HubRouteInfo hubRouteInfo = routeInfoClientService.getHubRouteInfo(command.departureId(),command.arrivalId());
        if(hubRouteInfo==null){
            log.warn("[RoutePlanService] 허브간 이동 정보 조회 실패 - departureHubId: {}, arrivalHubId: {} 존재하지 않음",
                command.departureId(), command.arrivalId());
            throw new BusinessException(RoutePlanErrorCode.HUB_ROUTE_INFO_NOT_FOUND);
        }

        log.debug("{}",departureHub);
        log.debug("{}",arrivalHub);
        log.debug("{}",hubRouteInfo);

        RoutePlan routePlan;
        //직행 경로 계획
        if(routePlanPolicy.isDirectAllowed(hubRouteInfo.distanceKm())){
            routePlan = RoutePlan.create(
                command.departureId(),
                command.arrivalId(),
                hubRouteInfo.durationMin(),
                hubRouteInfo.distanceKm()
            );
        }
        //경유 경로 계획
        else{
            List<HubRouteInfo> routeInfos = routeInfoClientService.getHubRouteInfos();
            routePlan = RoutePlan.plan(command.departureId(),command.arrivalId(),hubRouteInfo.toVo(),routeInfos.stream().map(HubRouteInfo::toVo).toList());
        }
        return new CreateRoutePlanResponse(routePlanRepository.save(routePlan).getRoutePlanId());
    }

    //출발 ID, 도착 ID로 조회
    @Transactional(readOnly = true)
    public GetRoutePlanDetailResponse getRoutePlan(UUID depId, UUID arrId) {
        RoutePlan routePlan= routePlanRepository.findByDepartureIdAndArrivalIdAndDeletedAtIsNull(depId,arrId)
            .orElseThrow(()->{
                log.warn("[RoutePlanService] 경로 계획 조회 실패 - departureHubId: {}, arrivalHubId: {} 존재하지 않음",depId,arrId);
                return new BusinessException(RoutePlanErrorCode.NO_ROUTE_PLAN_FOUND);
            });
        return GetRoutePlanDetailResponse.from(routePlan);
    }

    //경로 계획 ID로 조회
    @Transactional(readOnly = true)
    public GetRoutePlanDetailResponse getRoutePlan(UUID routePlanId) {
        RoutePlan routePlan = routePlanRepository.findByRoutePlanIdAndDeletedAtIsNull(routePlanId)
            .orElseThrow(()->{
                log.warn("[RoutePlanService] 경로 계획 조회 실패 - routPlanId: {} 존재하지 않음",routePlanId);
                return new BusinessException(RoutePlanErrorCode.NO_ROUTE_PLAN_FOUND);
            });
        return GetRoutePlanDetailResponse.from(routePlan);
    }
}
