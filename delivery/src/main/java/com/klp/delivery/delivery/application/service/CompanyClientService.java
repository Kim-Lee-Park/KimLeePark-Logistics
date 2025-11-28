package com.klp.delivery.delivery.application.service;

import com.klp.delivery.delivery.infrastructure.client.dto.CompanyResponse;

public interface CompanyClientService {

    CompanyResponse findCompany(String companyId);
}
