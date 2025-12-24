package com.klp.hub.company.application;

import com.klp.hub.company.application.dto.CreateCompanyCommand;
import com.klp.hub.company.domain.Company;
import com.klp.hub.company.domain.repository.CompanyRepository;
import com.klp.hub.company.exception.CompanyErrorCode;
import com.klp.hub.company.presentation.dto.response.CompanyListResponse;
import com.klp.hub.company.presentation.dto.response.CompanyResponse;
import com.klp.hub.company.presentation.dto.response.CreateCompanyResponse;
import com.klp.hub.global.exception.BusinessException;
import com.klp.hub.hub.application.service.HubService;
import com.klp.hub.hub.domain.model.Hub;
import java.util.List;
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

    private final HubService hubService;

    @Transactional(readOnly = true)
    public CompanyResponse getByCompanyId(UUID companyId) {
        Company company = companyRepository.findById(companyId).orElseThrow(() -> {
            log.error("업체를 찾을 수 없습니다. companyId : {}", companyId);
            return new BusinessException(CompanyErrorCode.NOT_FOUND_COMPANY);
        });

        return new CompanyResponse(
            company.getId(),
            company.getHubId(),
            company.getType().name(),
            company.getName(),
            company.getAddress()
        );
    }

    @Transactional(readOnly = true)
    public List<CompanyListResponse.CompanySummaryResponse> getAllByName(String name) {
        List<Company> companies = companyRepository.findAllByName(name);

        return companies.stream()
            .map(company -> new CompanyListResponse.CompanySummaryResponse(
                company.getId(),
                company.getName()
            ))
            .toList();
    }

    @Transactional
    public CreateCompanyResponse create(CreateCompanyCommand command) {
        Hub hub = hubService.getHubById(command.hubId());
        Company savedCompany = companyRepository.save(new Company(
            hub.getHubId(),
            command.type(),
            command.name(),
            command.address()
        ));

        return new CreateCompanyResponse(savedCompany.getId());
    }
}
