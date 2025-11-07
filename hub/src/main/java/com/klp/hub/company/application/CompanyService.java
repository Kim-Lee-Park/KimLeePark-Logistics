package com.klp.hub.company.application;

import com.klp.hub.company.domain.Company;
import com.klp.hub.company.domain.repository.CompanyRepository;
import com.klp.hub.company.presentation.dto.CompanyResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Transactional(readOnly = true)
    public CompanyResponse getByCompanyId(UUID companyId) {
        Company company = companyRepository.findById(companyId).orElseThrow(() -> {
            log.error("업체를 찾을 수 없습니다. companyId : {}", companyId);
            return new RuntimeException();
        });

        return new CompanyResponse(
            company.getId(),
            company.getHubId(),
            company.getType().name(),
            company.getName(),
            company.getAddress()
        );
    }
}
