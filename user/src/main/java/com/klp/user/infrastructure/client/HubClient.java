package com.klp.user.infrastructure.client;

import com.klp.user.global.config.HubFeignClientConfig;
import com.klp.user.infrastructure.client.dto.request.NearestHubRequest;
import com.klp.user.infrastructure.client.dto.response.GetHubIdResponse;
import com.klp.user.infrastructure.client.dto.response.HubDetailResponse;
import com.klp.user.infrastructure.client.dto.response.NearestHubResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "hub-service", configuration = HubFeignClientConfig.class)
public interface HubClient {

    @GetMapping("/v1/hubs/by-name")
    GetHubIdResponse getHubByName(@RequestParam("name") String hubName);

    @GetMapping("/v1/hubs/{hubId}")
    HubDetailResponse getHubById(@PathVariable("hubId") UUID hubId);

    @PostMapping("/v1/internal/hubs/nearest")
    NearestHubResponse getNearestHub(@RequestBody NearestHubRequest request);
}
