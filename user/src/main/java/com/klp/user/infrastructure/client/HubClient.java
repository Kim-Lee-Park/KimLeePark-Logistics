package com.klp.user.infrastructure.client;

import com.klp.user.infrastructure.client.dto.response.GetHubIdResponse;
import com.klp.user.infrastructure.client.dto.response.HubDetailResponse;
import java.util.UUID;
import com.klp.user.infrastructure.client.dto.request.NearestHubRequest;
import com.klp.user.infrastructure.client.dto.response.NearestHubResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "hub-service", contextId = "hubClient")
public interface HubClient {

    @GetMapping("/v1/hubs/by-name")
    GetHubIdResponse getHubByName(@RequestParam("name") String hubName);

    @GetMapping("/v1/hubs/{hubId}")
    HubDetailResponse getHubById(@PathVariable("hubId") UUID hubId);
    @PostMapping("/v1/internal/hubs/nearest")
    NearestHubResponse getNearestHub(@RequestBody NearestHubRequest request);
}
