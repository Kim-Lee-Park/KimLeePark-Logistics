package com.klp.delivery.routeplan.infrastructure.client;

import com.klp.delivery.common.exception.ExternalApiErrorCode;
import com.klp.delivery.common.exception.ExternalApiException;
import com.klp.delivery.routeplan.application.command.HubInfo;
import com.klp.delivery.routeplan.application.service.HubClientService;
import com.klp.delivery.routeplan.infrastructure.dto.HubResponse;
import feign.FeignException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class HubClientServiceImpl implements HubClientService {
    private final HubFeignClient hubFeignClient;

    @Override
    public HubInfo getHubById(UUID hubId) {
        try {
            HubResponse res = hubFeignClient.getHubById(hubId);
            return res.toCommand();
        } catch (FeignException.NotFound e) {
            return null;
        } catch (FeignException e) {
            log.error("[HubClientService] 외부 허브 서비스 호출 중 오류 - hubId: {}, status: {}, message: {}",
                hubId, e.status(), e.getMessage());
            throw new ExternalApiException(ExternalApiErrorCode.HUB_SERVICE_UNAVAILABLE);
        }
    }
}
