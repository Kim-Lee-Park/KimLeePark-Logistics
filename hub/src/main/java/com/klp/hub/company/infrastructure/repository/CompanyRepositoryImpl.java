package com.klp.hub.company.infrastructure.repository;

import com.klp.hub.company.domain.Company;
import com.klp.hub.company.domain.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CompanyRepositoryImpl implements CompanyRepository {
    private final CompanyJpaRepository companyJpaRepository;

    @Override
    public Optional<Company> findById(UUID companyId) {
        return companyJpaRepository.findById(companyId);
    }
}
