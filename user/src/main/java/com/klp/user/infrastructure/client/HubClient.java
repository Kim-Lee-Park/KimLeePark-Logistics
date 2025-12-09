package com.klp.user.infrastructure.client;

import com.klp.user.infrastructure.client.dto.request.NearestHubRequest;
import com.klp.user.infrastructure.client.dto.response.NearestHubResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "hub-service", contextId = "hubClient")
public interface HubClient {

    @PostMapping("/v1/internal/hubs/nearest")
    NearestHubResponse getNearestHub(@RequestBody NearestHubRequest request);
}
