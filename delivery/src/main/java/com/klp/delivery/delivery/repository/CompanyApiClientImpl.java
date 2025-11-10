package com.klp.delivery.delivery.repository;

import com.klp.delivery.delivery.application.service.CompanyApiClient;
import com.klp.delivery.delivery.domain.Company;
import org.springframework.stereotype.Component;


@Component
public class CompanyApiClientImpl implements CompanyApiClient {

  @Override
  public Company findCompany(String companyId) {
    return null;
  }
}
