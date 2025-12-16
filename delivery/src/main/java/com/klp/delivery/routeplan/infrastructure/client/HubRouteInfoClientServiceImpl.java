package com.klp.delivery.routeplan.infrastructure.client;

import com.klp.delivery.common.exception.ExternalApiErrorCode;
import com.klp.delivery.common.exception.ExternalApiException;
import com.klp.delivery.routeplan.application.command.HubRouteInfo;
import com.klp.delivery.routeplan.application.service.HubRouteInfoClientService;
import com.klp.delivery.routeplan.infrastructure.dto.HubRouteInfoResponse;
import com.klp.delivery.routeplan.infrastructure.dto.HubRouteInfoResponse.RouteInfoItem;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class HubRouteInfoClientServiceImpl implements HubRouteInfoClientService {

    private final HubFeignClient hubFeignClient;

    @Override
    @CircuitBreaker(name = "hubService")
    @Retry(name = "hubService")
    public HubRouteInfo getHubRouteInfo(UUID departureId, UUID arrivalId) {
        HubRouteInfoResponse res = hubFeignClient.getHubRouteInfo(departureId, arrivalId);

        try {
            if (!res.routeInfos().isEmpty()) {
                RouteInfoItem item = res.routeInfos().get(0);
                return item.toCommand();
            } else {
                return null;
            }
        } catch (FeignException.NotFound e) {
            return null;
        } catch (FeignException e) {
            log.error(
                "[HubRouteInfoClientService] 외부 허브 서비스 호출 중 오류 - departureId: {}, arrivalId: {}, status: {}, message: {}",
                departureId, arrivalId, e.status(), e.getMessage());
            throw new ExternalApiException(ExternalApiErrorCode.HUB_SERVICE_UNAVAILABLE);
        }
    }

    @Override
    @CircuitBreaker(name = "hubService")
    @Retry(name = "hubService")
    public List<HubRouteInfo> getHubRouteInfos() {
        HubRouteInfoResponse res = hubFeignClient.getHubRouteInfos();
        return res.routeInfos().stream().map(RouteInfoItem::toCommand).toList();
    }
}
