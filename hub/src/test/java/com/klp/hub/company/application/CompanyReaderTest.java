package com.klp.hub.company.application;

import com.klp.hub.company.domain.Company;
import com.klp.hub.company.domain.CompanyType;
import com.klp.hub.company.domain.repository.CompanyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyReaderTest {

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyReader companyReader;

    @Test
    @DisplayName("업체 ID 를 통해 업체를 조회할 수 있다")
    void getCompanyById() {
        UUID companyId = UUID.randomUUID();
        var company = mock(Company.class);
        when(company.getId()).thenReturn(companyId);
        when(company.getType()).thenReturn(CompanyType.SUPPLIER);
        when(company.getName()).thenReturn("업체명");
        when(company.getAddress()).thenReturn("업체주소");
        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        var response = companyReader.getByCompanyId(companyId);

        assertThat(response.id()).isNotNull();
    }

    @Test
    @DisplayName("해당 업체가 존재하지 않는다면 예외가 발생한다")
    void throwGetCompanyById() {
        when(companyRepository.findById(UUID.randomUUID())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> companyReader.getByCompanyId(UUID.randomUUID()));
    }
}
