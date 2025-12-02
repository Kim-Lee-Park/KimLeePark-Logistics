package com.klp.delivery.delivery.application.service;

import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_COMPANY_ADDRESS;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_COMPANY_NAME;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_RECEIVER_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createCompanyResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.klp.common.exception.BusinessException;
import com.klp.delivery.delivery.MockTest;
import com.klp.delivery.delivery.application.command.CompanyCommand;
import com.klp.delivery.delivery.exception.DeliveryErrorCode;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("CompanyService 테스트")
class CompanyServiceTest extends MockTest {

    @InjectMocks
    CompanyService companyService;

    @Mock
    CompanyClientService companyClientService;

    @Test
    @DisplayName("업체조회api_성공")
    void 업체조회api_성공() {
        // given: 업체 조회 데이터 준비
        UUID receiverId = DEFAULT_RECEIVER_ID;

        when(companyClientService.findCompany(receiverId.toString())).thenReturn(createCompanyResponse());

        // when: 업체 조회
        CompanyCommand result = companyService.findCompany(receiverId.toString());

        // then: 조회 검증
        verify(companyClientService, times(1)).findCompany(receiverId.toString());
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(DEFAULT_COMPANY_NAME);
        assertThat(result.address()).isEqualTo(DEFAULT_COMPANY_ADDRESS);
    }

    @ParameterizedTest
    @DisplayName("업체조회api_실패_예외발생 - 다양한 오류 상황")
    @CsvSource({
        "업체 조회 실패",
        "네트워크 오류",
        "타임아웃 발생"
    })
    void 업체조회api_실패_예외발생(String errorMessage) {
        // given: 업체 조회 데이터 준비
        UUID receiverId = DEFAULT_RECEIVER_ID;

        when(companyClientService.findCompany(anyString()))
            .thenThrow(new RuntimeException(errorMessage));

        // when & then: 예외 발생 검증
        assertThatThrownBy(() -> companyService.findCompany(receiverId.toString()))
            .isInstanceOf(BusinessException.class)
            .satisfies(exception -> {
                BusinessException businessException = (BusinessException) exception;
                assertThat(businessException.getErrorCode()).isEqualTo(
                    DeliveryErrorCode.EXTERNAL_API_ERROR);
            });

        // then: 외부 API 호출 검증
        verify(companyClientService, times(1)).findCompany(receiverId.toString());
    }
}

