package com.klp.delivery.delivery.infrastructure.client;

import com.klp.delivery.delivery.application.service.CompanyClientService;
import com.klp.delivery.delivery.infrastructure.client.dto.CompanyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class CompanyClienServicetImpl implements CompanyClientService {

    private final CompanyFeignClient companyFeignClient;

    @Override
    public CompanyResponse findCompany(String companyId) {
        return companyFeignClient.findCompany(companyId);
    }
}
