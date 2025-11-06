package com.klp.hub.company.domain.repository;

import com.klp.hub.company.domain.Company;

import java.util.Optional;
import java.util.UUID;

public interface CompanyRepository {
    Optional<Company> findById(UUID companyId);
}
