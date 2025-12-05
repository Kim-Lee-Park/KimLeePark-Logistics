package com.klp.hub.hub.application.facade;

import com.klp.hub.common.model.UserDetailsImpl;
import com.klp.hub.hub.application.command.hub.NearestHubCommand;
import com.klp.hub.hub.application.service.HubRouteInfoService;
import com.klp.hub.hub.application.service.HubService;
import com.klp.hub.hub.infrastructure.client.DeliveryFeignClient;
import com.klp.hub.hub.presentation.dto.response.NearestHubResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HubFacade {

    private final HubService hubService;
    private final HubRouteInfoService hubRouteInfoService;
    private final DeliveryFeignClient deliveryFeignClient;

    //허브 삭제 대기
    @Transactional
    public void markPendingDelete(UUID hubId, UserDetailsImpl userDetails) {
        //허브 삭제 대기 상태 변경
        hubService.markPendingDelete(hubId, userDetails);
        //허브간 이동 정보 삭제
        hubRouteInfoService.deleteHubRouteInfoByHubId(hubId, userDetails);
        //경로 계획 삭제
        //TODO: 이벤트로 변경하기
        deliveryFeignClient.deleteRoutePlansByHubId(hubId);
    }

    public NearestHubResponse getNearestHub(NearestHubCommand command) {
        return hubService.getNearestHub(command);
    }
}
