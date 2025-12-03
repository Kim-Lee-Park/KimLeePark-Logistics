package com.klp.delivery.delivery.application.service;

import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_ARRIVAL_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_ARRIVAL_NAME;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFALT_HUB_ADDRESS;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createArrivalHubInfo;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createArrivalInfoResponse;
import com.klp.delivery.delivery.infrastructure.client.dto.HubInfoResponse;

import static com.klp.delivery.delivery.fixture.DeliveryFixture.createDepartureHubInfo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.klp.common.exception.BusinessException;
import com.klp.delivery.delivery.MockTest;
import com.klp.delivery.delivery.application.command.HubInfoCommand;
import com.klp.delivery.delivery.exception.DeliveryErrorCode;
import com.klp.delivery.routeplan.application.service.HubClientService;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("HubService 테스트")
class HubServiceTest extends MockTest {

    @InjectMocks
    HubService hubService;

    @Mock
    HubClientService hubClientService;

    @Test
    @DisplayName("허브조회api_성공")
    void 허브조회api_성공() {
        // given: 허브 조회 데이터 준비

        when(hubClientService.getHubById(DEFAULT_ARRIVAL_ID)).thenReturn(createArrivalHubInfo());

        // when: 허브 조회
        HubInfoCommand result = hubService.findHubInfo(DEFAULT_ARRIVAL_ID);

        // then: 조회 검증
        verify(hubClientService, times(1)).getHubById(DEFAULT_ARRIVAL_ID);
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(DEFAULT_ARRIVAL_NAME);
        assertThat(result.address()).isEqualTo(DEFALT_HUB_ADDRESS);
    }

    @ParameterizedTest
    @DisplayName("허브조회api_실패_예외발생 - 다양한 오류 상황")
    @CsvSource({
        "허브 조회 실패",
        "네트워크 오류",
        "타임아웃 발생"
    })
    void 허브조회api_실패_예외발생(String errorMessage) {
        // given: 허브 조회 데이터 준비
        when(hubClientService.getHubById(DEFAULT_ARRIVAL_ID))
            .thenThrow(new RuntimeException(errorMessage));

        // when & then: 예외 발생 검증
        assertThatThrownBy(() -> hubService.findHubInfo(DEFAULT_ARRIVAL_ID))
            .isInstanceOf(BusinessException.class)
            .satisfies(exception -> {
                BusinessException businessException = (BusinessException) exception;
                assertThat(businessException.getErrorCode()).isEqualTo(
                    DeliveryErrorCode.EXTERNAL_API_ERROR);
            });

        // then: 외부 API 호출 검증
        verify(hubClientService, times(1)).getHubById(DEFAULT_ARRIVAL_ID);
    }
}

