package com.klp.delivery.delivery.application.service;

import com.klp.delivery.delivery.domain.Company;

public interface CompanyApiClient {

  Company findCompany(String companyId);
}
