package com.klp.delivery.delivery.infrastructure.client;

import com.klp.delivery.delivery.application.service.CompanyApiClient;
import com.klp.delivery.delivery.application.command.CompanyCommand;
import org.springframework.stereotype.Component;


@Component
public class CompanyApiClientImpl implements CompanyApiClient {

  @Override
  public CompanyCommand findCompany(String companyId) {
    return null;
  }
}
