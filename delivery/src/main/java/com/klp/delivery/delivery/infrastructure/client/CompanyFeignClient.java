package com.klp.delivery.delivery.infrastructure.client;

import com.klp.delivery.delivery.infrastructure.client.dto.CompanyResponse;
import com.klp.delivery.global.config.CompanyFeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "hub-service", contextId = "companyFeignClient",
    configuration = CompanyFeignClientConfig.class)
public interface CompanyFeignClient {

    @GetMapping("/v1/companies/{companyId}")
    CompanyResponse findCompany(@PathVariable("companyId") String companyId);

}
