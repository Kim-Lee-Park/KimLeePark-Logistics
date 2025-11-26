package com.klp.delivery.routeplan.application.scheduler;

import com.klp.delivery.common.enums.RoutePlanStatus;
import com.klp.delivery.routeplan.application.service.DeliveryClientService;
import com.klp.delivery.routeplan.domain.model.RoutePlan;
import com.klp.delivery.routeplan.domain.repository.RoutePlanRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoutePlanDeleteScheduler {

    private final RoutePlanRepository routePlanRepository;
    private final DeliveryClientService deliveryClientService;

    /*
     * 삭제 대기인 RoutePlan 중에서 진행중인 배송이 없는 것들을 정리
     * 3시간 주기로 실행
     */
    @Scheduled(
        fixedRateString = "${scheduler.route-plan-delete.fixed-rate}",
        initialDelayString = "${scheduler.route-plan-delete.initial-delay}"
    )
    @Transactional
    public void cleanPendingDeleteRoutePlans() {
        log.info("[RoutePlanPendingDeleteScheduler] 시작 - PENDING_DELETE RoutePlan 정리");

        List<RoutePlan> pendingDeleteRoutePlans =
            routePlanRepository.findAllByStatus(RoutePlanStatus.PENDING_DELETE);

        for (RoutePlan routePlan : pendingDeleteRoutePlans) {
            UUID routePlanId = routePlan.getRoutePlanId();

            boolean hasActiveDeliveries =
                deliveryClientService.hasActiveDeliveries(routePlanId);

            if (hasActiveDeliveries) {
                log.info("[RoutePlanPendingDeleteScheduler] 아직 사용 중인 RoutePlan, 보류 routePlanId={}",
                    routePlanId);
                continue;
            }

            routePlan.softDelete();
        }

        log.info("[RoutePlanPendingDeleteScheduler] 종료 - PENDING_DELETE RoutePlan 정리");
    }
}