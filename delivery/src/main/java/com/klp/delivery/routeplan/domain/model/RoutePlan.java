package com.klp.delivery.routeplan.domain.model;

import com.klp.delivery.common.entity.BaseEntity;
import com.klp.delivery.common.enums.RoutePlanStatus;
import com.klp.delivery.global.exception.BusinessException;
import com.klp.delivery.routeplan.domain.vo.PlanDetailVo;
import com.klp.delivery.routeplan.domain.vo.RouteInfoVo;
import com.klp.delivery.routeplan.exception.RoutePlanErrorCode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Entity
@Table(name = "p_route_plans",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_route_plan_departure_arrival",
            columnNames = {"departure_id", "arrival_id"}
        )
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class RoutePlan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID routePlanId;

    @Column(nullable = false)
    private UUID departureId;
    @Column(nullable = false)
    private String departureName;
    @Column(nullable = false)
    private UUID arrivalId;
    @Column(nullable = false)
    private String arrvalName;

    @Column(nullable = false)
    private Long totalDurationMin;
    @Column(nullable = false)
    private Double totalDistanceKm;

    @OneToMany(mappedBy = "routePlan", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoutePlanItem> routePlanItems = new ArrayList<>();

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RoutePlanStatus status;

    //직행 경로 계획
    public static RoutePlan create(UUID departureId, String departureName,
        UUID arrivalId, String arrivalName, long totalDurationMin,
        double totalDistanceKm) {
        validateCreateParam(departureId, arrivalId, totalDurationMin, totalDistanceKm);
        RoutePlan routePlan = new RoutePlan();
        routePlan.departureId = departureId;
        routePlan.departureName = departureName;
        routePlan.arrivalId = arrivalId;
        routePlan.arrvalName = arrivalName;
        routePlan.totalDurationMin = totalDurationMin;
        routePlan.totalDistanceKm = totalDistanceKm;
        routePlan.routePlanItems.add(
            RoutePlanItem.create(departureId, departureName, arrivalId, arrivalName,
                totalDurationMin, totalDistanceKm, 1,
                routePlan));
        routePlan.status = RoutePlanStatus.ACTIVE;
        return routePlan;
    }

    private static void validateCreateParam(UUID departureId, UUID arrivalId, long totalDurationMin,
        double totalDistanceKm) {
        if (departureId == null) {
            throw new BusinessException(RoutePlanErrorCode.DEPARTURE_ID_REQUIRED);
        }
        if (arrivalId == null) {
            throw new BusinessException(RoutePlanErrorCode.ARRIVAL_ID_REQUIRED);
        }
        if (totalDurationMin < 0) {
            throw new BusinessException(RoutePlanErrorCode.DURATION_INVALID);
        }
        if (totalDistanceKm < 0) {
            throw new BusinessException(RoutePlanErrorCode.DISTANCE_INVALID);
        }
    }

    //경유 경로 계획
    public static RoutePlan plan(UUID originId, String originName, UUID destinationId,
        String destinationName,
        RouteInfoVo directRoute, List<RouteInfoVo> routeInfos, Map<UUID, String> hubNameMap) {
        validatePlanParam(originId, destinationId, routeInfos);
        /*
            키: 출발허브ID, 값: 출발 허브ID인 이동 정보
            출발 허브의 도착 가능한 모든 허브 이동 정보
        * */
        Map<UUID, List<RouteInfoVo>> routeInfoMap = new HashMap<>();
        Set<UUID> hubIdSet = new HashSet<>();
        routeInfos.forEach(routeInfo -> {
            if (routeInfo.equals(directRoute)) {
                return;
            }
            routeInfoMap.computeIfAbsent(routeInfo.departureId(), k -> new ArrayList<>())
                .add(routeInfo);
            hubIdSet.add(routeInfo.departureId());
            hubIdSet.add(routeInfo.arrivalId());
        });

        /*
         * 키: 도착허브ID, 값: 경로 계획 요약 정보
         * {경로 구간 정보 list, 총 시간, 총 거리}
         * */
        Map<UUID, PlanDetailVo> planDetailMap = new HashMap<>();
        hubIdSet.forEach(hubId -> {
            planDetailMap.put(hubId,
                new PlanDetailVo(hubId, new ArrayList<>(), Long.MAX_VALUE, 0.));
        });
        planDetailMap.put(originId, new PlanDetailVo(originId, new ArrayList<>(), 0L, 0.));

        PriorityQueue<PlanDetailVo> planPriorityQ = new PriorityQueue<>();
        planPriorityQ.add(planDetailMap.get(originId));

        while (!planPriorityQ.isEmpty()) {
            PlanDetailVo now = planPriorityQ.poll();
            if (now.totalDurationMin() > planDetailMap.get(now.hubId()).totalDurationMin()) {
                continue;
            }

            List<RouteInfoVo> nextRoutes = routeInfoMap.get(now.hubId());
            if (nextRoutes == null) {
                continue;
            }

            for (RouteInfoVo next : nextRoutes) {
                PlanDetailVo bestPlan = planDetailMap.get(next.arrivalId());
                Long duration = now.totalDurationMin() + next.durationMin();
                Double distance = now.totalDistanceKm() + next.distanceKm();
                if (bestPlan.totalDurationMin() > duration) {
                    renewBestPlan(planDetailMap, planPriorityQ, now, next, duration, distance);
                } else if (bestPlan.totalDurationMin().equals(duration)) {
                    if (bestPlan.totalDistanceKm() > distance) {
                        renewBestPlan(planDetailMap, planPriorityQ, now, next, duration, distance);
                    }
                }
            }
        }

        PlanDetailVo bestPlan = planDetailMap.get(destinationId);
        if (bestPlan == null) {
            log.warn(
                "경로 계획에 실패했습니다. 출발지(originId)={}, 도착지(destinationId)={}",
                originId,
                destinationId
            );
            throw new BusinessException(RoutePlanErrorCode.NO_ROUTE_PLAN_FOUND);
        }

        RoutePlan routePlan = new RoutePlan();
        routePlan.departureId = originId;
        routePlan.departureName = originName;
        routePlan.arrivalId = destinationId;
        routePlan.arrvalName = destinationName;
        routePlan.totalDurationMin = bestPlan.totalDurationMin();
        routePlan.totalDistanceKm = bestPlan.totalDistanceKm();
        routePlan.status = RoutePlanStatus.ACTIVE;

        //경로 계획 구간 정보들
        List<RouteInfoVo> planItemVos = bestPlan.planItems();
        for (int i = 0; i < planItemVos.size(); i++) {
            RouteInfoVo info = planItemVos.get(i);
            String departureName = hubNameMap.get(info.departureId());
            String arrivalName = hubNameMap.get(info.arrivalId());
            RoutePlanItem routePlanItem = RoutePlanItem.from(info, i + 1, routePlan, departureName,
                arrivalName);
            routePlan.routePlanItems.add(routePlanItem);
        }

        return routePlan;
    }

    private static void renewBestPlan(Map<UUID, PlanDetailVo> planDetailMap,
        PriorityQueue<PlanDetailVo> planPriorityQ, PlanDetailVo now, RouteInfoVo next,
        Long duration, Double distance) {
        List<RouteInfoVo> planItems = new ArrayList<>(now.planItems());
        planItems.add(next);

        PlanDetailVo newBestPlan = new PlanDetailVo(next.arrivalId(),
            planItems,
            duration,
            distance);

        planDetailMap.put(next.arrivalId(), newBestPlan);
        planPriorityQ.add(newBestPlan);
    }

    private static void validatePlanParam(UUID originId, UUID destinationId,
        List<RouteInfoVo> routeInfos) {
        if (originId == null) {
            throw new BusinessException(RoutePlanErrorCode.DEPARTURE_ID_REQUIRED);
        }
        if (destinationId == null) {
            throw new BusinessException(RoutePlanErrorCode.ARRIVAL_ID_REQUIRED);
        }
        if (routeInfos == null || routeInfos.isEmpty()) {
            throw new BusinessException(RoutePlanErrorCode.ROUTE_INFOS_NEEDED);
        }
    }

    public void pendingDelete(Long userId) {
        this.status = RoutePlanStatus.PENDING_DELETE;
        this.setDeletedBy(userId);
    }

    public void softDelete() {
        if (!this.status.isPendingDelete()) {
            return;
        }

        this.status = RoutePlanStatus.DELETED;
        this.setDeletedAt(LocalDateTime.now());
        this.getRoutePlanItems().forEach(routePlanItem -> {
            routePlanItem.delete(0L);
        });
    }
}
