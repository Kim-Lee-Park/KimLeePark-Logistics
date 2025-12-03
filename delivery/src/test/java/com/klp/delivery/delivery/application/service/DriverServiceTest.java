package com.klp.delivery.delivery.application.service;

import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_ARRIVAL_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createDriversResponse;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createDriversResponses;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.klp.common.exception.BusinessException;
import com.klp.delivery.delivery.MockTest;
import com.klp.delivery.delivery.application.command.DriverCommand;
import com.klp.delivery.delivery.exception.DeliveryErrorCode;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("DriverService 테스트")
class DriverServiceTest extends MockTest {

    @InjectMocks
    DriverService driverService;

    @Mock
    DriverClientService driverClientService;

    @Test
    @DisplayName("담당자조회api_성공 - 도착 허브의 배송 담당자들 조회")
    void 담당자조회api_성공() {
        // given: 담당자 조회 데이터 준비
        UUID hubId = DEFAULT_ARRIVAL_ID;

        when(driverClientService.findArrivalHubDrivers(hubId)).thenReturn(createDriversResponses());

        // when: 담당자 조회
        List<DriverCommand> result = driverService.findArrivalHubDrivers(hubId);

        // then: 조회 검증
        verify(driverClientService, times(1)).findArrivalHubDrivers(hubId);
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).userId()).isEqualTo(createDriversResponse().userId());
    }

    @Test
    @DisplayName("담당자조회api_실패_예외발생 - 외부 API 호출 실패 시 예외 처리")
    void 담당자조회api_실패_예외발생() {
        // given: 담당자 조회 데이터 준비
        UUID hubId = DEFAULT_ARRIVAL_ID;

        when(driverClientService.findArrivalHubDrivers(hubId))
            .thenThrow(new RuntimeException("담당자 조회 실패"));

        // when & then: 예외 발생 검증
        assertThatThrownBy(() -> driverService.findArrivalHubDrivers(hubId))
            .isInstanceOf(BusinessException.class)
            .satisfies(exception -> {
                BusinessException businessException = (BusinessException) exception;
                assertThat(businessException.getErrorCode()).isEqualTo(
                    DeliveryErrorCode.EXTERNAL_API_ERROR);
            });

        // then: 외부 API 호출 검증
        verify(driverClientService, times(1)).findArrivalHubDrivers(hubId);
    }

    @Test
    @DisplayName("특정 담당자 조회 성공")
    void findDriverAtArrivalHub_성공() {
        // given
        Long vendorDriverId = 1234L;
        when(driverClientService.findDriverAtArrivalHub(vendorDriverId))
            .thenReturn(createDriversResponse());

        // when
        DriverCommand result = driverService.findDriverAtArrivalHub(vendorDriverId);

        // then
        verify(driverClientService, times(1)).findDriverAtArrivalHub(vendorDriverId);
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(createDriversResponse().userId());
    }

    @Test
    @DisplayName("물류 배송 담당자들 조회 성공")
    void findLogisticsDrivers_성공() {
        // given
        when(driverClientService.findLogisticsDrivers()).thenReturn(createDriversResponses());

        // when
        List<DriverCommand> result = driverService.findLogisticsDrivers();

        // then
        verify(driverClientService, times(1)).findLogisticsDrivers();
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
    }
}

