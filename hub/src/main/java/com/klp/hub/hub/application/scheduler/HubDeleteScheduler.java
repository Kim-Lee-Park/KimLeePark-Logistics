package com.klp.hub.hub.application.scheduler;

import com.klp.hub.hub.application.service.OrderClientService;
import com.klp.hub.hub.domain.model.Hub;
import com.klp.hub.hub.domain.model.HubStatus;
import com.klp.hub.hub.domain.repository.HubRepository;
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
public class HubDeleteScheduler {

    private final HubRepository hubRepository;
    private final OrderClientService orderClientService;

    /*
     * 삭제 대기인 Hub 중에서 진행중인 주문이 없는 것들을 정리
     * 3시간 주기로 실행
     */
    @Scheduled(
        fixedRateString = "${scheduler.hub-delete.fixed-rate}",
        initialDelayString = "${scheduler.hub-delete.initial-delay}"
    )
    @Transactional
    public void cleanPendingDeleteHubs() {
        log.info("[HubDeleteScheduler] 시작 - PENDING_DELETE Hub 정리");

        List<Hub> pendingDeleteHubs =
            hubRepository.findAllByStatus(HubStatus.PENDING_DELETE);

        for (Hub hub : pendingDeleteHubs) {
            UUID hubId = hub.getHubId();

            boolean hasProgressingOrders = orderClientService.hasProgressingOrders(hubId);

            if (hasProgressingOrders) {
                log.info("[HubDeleteScheduler] 아직 사용 중인 Hub, 보류 hubId={}",
                    hubId);
                continue;
            }

            hub.softDelete();
        }

        log.info("[HubDeleteScheduler] 종료 - PENDING_DELETE Hub 정리");
    }

}
