package com.klp.delivery.routeplan.infrastructure.client;

import com.klp.delivery.global.config.FeignTracingConfig;
import com.klp.delivery.global.config.HubFeignClientConfig;
import com.klp.delivery.routeplan.infrastructure.dto.HubResponse;
import com.klp.delivery.routeplan.infrastructure.dto.HubRouteInfoResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Component
@FeignClient(name = "hub-service", configuration = {HubFeignClientConfig.class,
    FeignTracingConfig.class})
public interface HubFeignClient {

    @GetMapping("/v1/hubs/{hubId}")
    HubResponse getHubById(@PathVariable UUID hubId);

    @GetMapping("/v1/hubs/routes/info")
    HubRouteInfoResponse getHubRouteInfo(@RequestParam UUID departureId,
        @RequestParam UUID arrivalId);

    @GetMapping("/v1/hubs/routes/info/all")
    HubRouteInfoResponse getHubRouteInfos();
}
