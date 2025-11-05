package com.klp.hub.company.application;

import com.klp.hub.company.domain.repository.CompanyRepository;
import com.klp.hub.company.presentation.dto.CompanyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class CompanyReader {

    private final CompanyRepository companyRepository;

    public CompanyResponse getByCompanyId(UUID companyId) {
        return null;
    }
}
