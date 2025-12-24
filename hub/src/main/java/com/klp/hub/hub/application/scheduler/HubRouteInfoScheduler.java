package com.klp.hub.hub.application.scheduler;

import com.klp.hub.global.exception.BusinessException;
import com.klp.hub.hub.application.service.HubService;
import com.klp.hub.hub.domain.model.Hub;
import com.klp.hub.hub.domain.model.HubRouteInfo;
import com.klp.hub.hub.domain.repository.HubRepository;
import com.klp.hub.hub.domain.repository.HubRouteInfoRepository;
import com.klp.hub.hub.infrastructure.dto.RoutePairDto;
import com.klp.hub.hub.util.DistanceTimeUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class HubRouteInfoScheduler {

    private final HubRepository hubRepository;
    private final HubRouteInfoRepository routeInfoRepository;
    private final HubService hubService;
    private final HubRouteInfoRepository hubRouteInfoRepository;

    //기본 설정 2시간마다
    @Scheduled(
        fixedRateString = "${scheduler.route-infos.fixed-rate}",
        initialDelayString = "${scheduler.route-infos.initial-delay}"
    )
    // 멀티 서버, 멀티 스레드 환경에서도 스케줄러를 하나만 실행 / lock 유지 최소, 최대 시간
    @SchedulerLock(
        name = "generateMissingHubRouteInfos",
        lockAtLeastFor = "${shedlock.route-infos.at-least}",
        lockAtMostFor = "${shedlock.route-infos.at-most}"
    )
    public void generateMissingRoutes() {
        log.info("[{}] HubRouteInfoScheduler started at {}", LocalDateTime.now(),
            Thread.currentThread().getName());
        long start = System.currentTimeMillis();
        int createdCount = 0;

        List<UUID> hubIds = hubRepository.getActiveHubIds();
        if (hubIds.size() < 2) {
            return;
        }

        Set<RoutePairDto> existing = new HashSet<>(routeInfoRepository.findExistingPairsIn(hubIds));

        // 전체 순서쌍 생성 → 차집합으로 미존재 목록 계산
        List<RoutePairDto> missing = new ArrayList<>();
        for (UUID dep : hubIds) {
            for (UUID arr : hubIds) {
                RoutePairDto pair = new RoutePairDto(dep, arr);
                if (!existing.contains(pair)) {
                    missing.add(pair);
                }
            }
        }
        log.debug("missing size : {}", missing.size());

        Map<UUID, Hub> hubCache = hubService.getHubByIds(hubIds).stream()
            .collect(Collectors.toMap(Hub::getHubId, Function.identity()));

        for (RoutePairDto pair : missing) {
            try {
                Hub departureHub = hubCache.get(pair.departureId());
                Hub arrivalHub = hubCache.get(pair.arrivalId());

                Long durationMin = DistanceTimeUtil.estimateDurationMinutes(
                    departureHub.getLatitude(), departureHub.getLongitude(),
                    arrivalHub.getLatitude(), arrivalHub.getLongitude());
                Double distanceKm = DistanceTimeUtil.calculateDistanceKm(departureHub.getLatitude(),
                    departureHub.getLongitude(), arrivalHub.getLatitude(),
                    arrivalHub.getLongitude());

                HubRouteInfo routeInfo = HubRouteInfo.create(pair.departureId(), pair.arrivalId(),
                    durationMin, distanceKm);
                hubRouteInfoRepository.save(routeInfo);
                createdCount++;
            } catch (BusinessException e) {
                log.warn("ErrorCode : {}, Message: {}", e.getErrorCode(), e.getMessage());
            } catch (DataIntegrityViolationException e) {
                log.warn("유니크 제약 조건 위반 departureId: {}, arrivalId: {}", pair.departureId(),
                    pair.arrivalId());
            } catch (Exception e) {
                log.warn("Route create failed: {} -> {}", pair.departureId(), pair.arrivalId(), e);
            }
        }

        long elapsed = System.currentTimeMillis() - start;
        log.info("[HubRouteInfoScheduler] finished at {} — created {} new routes in {} ms",
            LocalDateTime.now(), createdCount, elapsed);
    }
}
