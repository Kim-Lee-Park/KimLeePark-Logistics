package com.klp.hub.company.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.klp.hub.company.domain.Company;
import com.klp.hub.company.domain.CompanyType;
import com.klp.hub.company.domain.repository.CompanyRepository;
import com.klp.hub.company.presentation.dto.CompanyResponse;
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
        when(companyRepository.findById(UUID.randomUUID())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
            () -> companyService.getByCompanyId(UUID.randomUUID()));
    }
}
