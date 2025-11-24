package com.klp.delivery.delivery.application.service;

import com.klp.delivery.delivery.application.command.CompanyCommand;

public interface CompanyApiClient {

    CompanyCommand findCompany(String companyId);
}
