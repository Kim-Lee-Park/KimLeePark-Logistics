package com.klp.user.infrastructure.client;

import com.klp.user.global.config.CompanyFeignClientConfig;
import com.klp.user.infrastructure.client.dto.response.CompanyListResponse;
import com.klp.user.infrastructure.client.dto.response.CompanyResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "hub-service", configuration = CompanyFeignClientConfig.class)
public interface CompanyClient {

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
