package com.klp.user.infrastructure.client;

import com.klp.user.global.config.FeignTracingConfig;
import com.klp.user.global.config.HubFeignClientConfig;
import com.klp.user.infrastructure.client.dto.request.NearestHubRequest;
import com.klp.user.infrastructure.client.dto.response.CompanyListResponse;
import com.klp.user.infrastructure.client.dto.response.CompanyResponse;
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

@FeignClient(name = "hub-service", url = "${clients.hub.url:}", configuration = {HubFeignClientConfig.class,
    FeignTracingConfig.class})
public interface HubClient {

    /**
     * Hub 이름으로 hubId를 조회합니다.
     */
    @GetMapping("/v1/hubs/by-name")
    GetHubIdResponse getHubByName(@RequestParam("name") String hubName);

    /**
     * Hub Id으로 hub 상세정보를 조회합니다.
     */
    @GetMapping("/v1/hubs/{hubId}")
    HubDetailResponse getHubById(@PathVariable("hubId") UUID hubId);

    /**
     * 주소지의 위도 경도를 기준으로 가장 가까운 허브Id를 찾습니다.
     */
    @PostMapping("/v1/internal/hubs/nearest")
    NearestHubResponse getNearestHub(@RequestBody NearestHubRequest request);

    /**
     * 업체 ID로 업체 정보를 조회합니다.
     */
    @GetMapping("/v1/companies/{companyId}")
    CompanyResponse getCompanyById(@PathVariable("companyId") UUID companyId);

    /**
     * 업체 이름으로 업체 목록을 조회합니다.
     */
    @GetMapping("/v1/companies")
    CompanyListResponse getCompaniesByName(@RequestParam("name") String name);
}
