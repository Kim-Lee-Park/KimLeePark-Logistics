package com.klp.hub.company.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.klp.hub.company.application.dto.CreateCompanyCommand;
import com.klp.hub.company.domain.Company;
import com.klp.hub.company.domain.CompanyType;
import com.klp.hub.company.domain.repository.CompanyRepository;
import com.klp.hub.company.exception.CompanyErrorCode;
import com.klp.hub.company.presentation.dto.response.CompanyListResponse;
import com.klp.hub.company.presentation.dto.response.CompanyResponse;
import com.klp.hub.global.exception.BusinessException;
import com.klp.hub.global.exception.ErrorCode;
import com.klp.hub.hub.application.service.HubService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private HubService hubService;

    @InjectMocks
    private CompanyService companyService;

    @Test
    @DisplayName("업체 ID 를 통해 업체를 조회할 수 있다")
    void getCompanyById() {
        UUID companyId = UUID.randomUUID();
        Company company = mock(Company.class);
        when(company.getId()).thenReturn(companyId);
        when(company.getType()).thenReturn(CompanyType.SUPPLIER);
        when(company.getName()).thenReturn("업체명");
        when(company.getAddress()).thenReturn("업체주소");
        when(companyRepository.findById(companyId))
            .thenReturn(Optional.of(company));

        CompanyResponse response = companyService.getByCompanyId(companyId);

        assertThat(response.companyId()).isNotNull();
    }

    @Test
    @DisplayName("해당 업체가 존재하지 않는다면 예외가 발생한다")
    void throwGetCompanyById() {
        UUID companyId = UUID.randomUUID();
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        ErrorCode errorCode = assertThrows(BusinessException.class,
            () -> companyService.getByCompanyId(companyId))
            .getErrorCode();
        assertEquals(CompanyErrorCode.NOT_FOUND_COMPANY, errorCode);
    }

    @Test
    @DisplayName("업체명을 통해 업체 목록을 조회할 수 있다")
    void getAllByName() {
        String name = "업체A";
        UUID companyId1 = UUID.randomUUID();
        UUID companyId2 = UUID.randomUUID();
        Company company1 = mock(Company.class);
        when(company1.getId()).thenReturn(companyId1);
        when(company1.getName()).thenReturn(name);
        Company company2 = mock(Company.class);
        when(company2.getId()).thenReturn(companyId2);
        when(company2.getName()).thenReturn(name);
        when(companyRepository.findAllByName(name))
            .thenReturn(List.of(company1, company2));

        List<CompanyListResponse.CompanySummaryResponse> result = companyService.getAllByName(name);

        assertThat(result)
            .hasSize(2)
            .extracting(CompanyListResponse.CompanySummaryResponse::companyId)
            .containsExactlyInAnyOrder(companyId1, companyId2);
        assertThat(result)
            .extracting(CompanyListResponse.CompanySummaryResponse::companyName)
            .allMatch(n -> n.equals(name));
        verify(companyRepository).findAllByName(name);
    }

    @Test
    @DisplayName("해당 업체명이 존재하지 않는다면 빈 리스트를 반환한다")
    void getCompaniesByNameIsEmpty() {
        String name = "없는 업체명";
        when(companyRepository.findAllByName(name))
            .thenReturn(List.of());

        List<CompanyListResponse.CompanySummaryResponse> result = companyService.getAllByName(name);

        assertTrue(result.isEmpty());
        verify(companyRepository).findAllByName(name);
    }

    @Test
    @DisplayName("업체 생성시 허브가 존재하지 않는다면 예외가 발생한다")
    void createCompanyThrowNotFoundHub() {
        UUID hubId = UUID.randomUUID();
        CreateCompanyCommand command = new CreateCompanyCommand(
            hubId,
            CompanyType.SUPPLIER,
            "업체명",
            "업체 주소"
        );
        when(hubService.getHubById(hubId))
            .thenThrow(BusinessException.class);

        assertThrows(BusinessException.class, () -> companyService.create(command));
    }
}
