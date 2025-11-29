package com.klp.delivery.delivery.application.service;

import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_ARRIVAL_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_DEPARTURE_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_DELIVERY_ID_FIRST;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_VENDOR_DRIVER_ID;
import static com.klp.delivery.routeplan.fixture.RoutePlanFixture.ARRIVAL_ID;
import static com.klp.delivery.routeplan.fixture.RoutePlanFixture.DEPARTURE_ID;
import static com.klp.delivery.routeplan.fixture.RoutePlanFixture.ROUTE_PLAN_ID;
import static com.klp.delivery.routeplan.fixture.RoutePlanFixture.TOTAL_DISTANCE;
import static com.klp.delivery.routeplan.fixture.RoutePlanFixture.TOTAL_DURATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.klp.common.exception.BusinessException;
import com.klp.delivery.common.enums.DeliveryStatus;
import com.klp.delivery.delivery.MockTest;
import com.klp.delivery.delivery.application.command.DeliveryRouteCommand;
import com.klp.delivery.delivery.application.command.DeliveryRoutePlanCommand;
import com.klp.delivery.delivery.application.command.DeliveryRouteStatusCommand;
import com.klp.delivery.delivery.application.service.DriverClientService;
import com.klp.delivery.delivery.domain.entity.DeliveryRoute;
import com.klp.delivery.delivery.domain.repository.DeliveryRouteRepository;
import com.klp.delivery.delivery.exception.DeliveryErrorCode;
import com.klp.delivery.delivery.infrastructure.client.dto.DriverResponse;
import com.klp.delivery.routeplan.exception.RoutePlanErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("배송 경로 서비스 테스트")
class DeliveryRouteServiceTest extends MockTest {

    @InjectMocks
    DeliveryRouteService deliveryRouteService;

    @Mock
    DeliveryRouteRepository deliveryRouteRepository;

    @Mock
    DriverClientService driverClientService;

    @Test
    @DisplayName("RoutePlanItem 없을 때 - 중간 허브 없음, 바로 최종 허브 도착 (ARRIVED_AT_FINAL_HUB)")
    void createDeliveryRoute_RoutePlanItem없음_직행_성공() {
        // given: RoutePlanItem이 없는 경우 (직행 경로)
        DeliveryRouteCommand deliveryCommand = new DeliveryRouteCommand(
            DEFAULT_DELIVERY_ID_FIRST,
            DEFAULT_DEPARTURE_ID,
            DEFAULT_ARRIVAL_ID,
            DEFAULT_VENDOR_DRIVER_ID
        );

        DeliveryRoutePlanCommand planCommand = new DeliveryRoutePlanCommand(
            ROUTE_PLAN_ID,
            DEFAULT_DEPARTURE_ID,
            DEFAULT_ARRIVAL_ID,
            TOTAL_DURATION,
            TOTAL_DISTANCE,
            new ArrayList<>() // planItems가 비어있음 (직행)
        );

        DeliveryRoute savedRoute = DeliveryRoute.create(
            DEFAULT_DELIVERY_ID_FIRST,
            DEFAULT_VENDOR_DRIVER_ID,
            DEFAULT_DEPARTURE_ID,
            DEFAULT_ARRIVAL_ID,
            1,
            TOTAL_DISTANCE,
            TOTAL_DURATION,
            TOTAL_DISTANCE,
            TOTAL_DURATION,
            DeliveryStatus.ARRIVED_AT_FINAL_HUB
        );

        when(deliveryRouteRepository.save(any(DeliveryRoute.class))).thenReturn(savedRoute);

        // when
        DeliveryRouteStatusCommand result = deliveryRouteService.createDeliveryRoute(
            deliveryCommand, planCommand);

        // then: 상태가 ARRIVED_AT_FINAL_HUB이고, vendorDriverId가 사용되었는지 검증
        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(DeliveryStatus.ARRIVED_AT_FINAL_HUB);
        assertThat(result.deliveryRouteId()).isEqualTo(savedRoute.getDeliveryRouteId());

        verify(deliveryRouteRepository, times(1)).save(any(DeliveryRoute.class));
        verify(driverClientService, times(0)).findLogisticsDrivers(); // 물류 담당자 조회 안함
    }

    @Test
    @DisplayName("RoutePlanItem 있을 때 - 중간 허브 있음, 첫 번째 아이템 사용 (IN_HUB_TRANSIT)")
    void createDeliveryRoute_RoutePlanItem있음_경유_성공() {
        // given: RoutePlanItem이 있는 경우 (경유 경로)
        UUID midHubId = UUID.randomUUID();
        UUID routePlanItemId = UUID.randomUUID();

        DeliveryRouteCommand deliveryCommand = new DeliveryRouteCommand(
            DEFAULT_DELIVERY_ID_FIRST,
            DEFAULT_DEPARTURE_ID,
            DEFAULT_ARRIVAL_ID,
            DEFAULT_VENDOR_DRIVER_ID
        );

        // 첫 번째 PlanItem 생성
        DeliveryRoutePlanCommand.PlanItem firstPlanItem = new DeliveryRoutePlanCommand.PlanItem(
            routePlanItemId,
            ROUTE_PLAN_ID,
            DEFAULT_DEPARTURE_ID,
            midHubId,
            20L,
            50.0,
            1
        );

        DeliveryRoutePlanCommand planCommand = new DeliveryRoutePlanCommand(
            ROUTE_PLAN_ID,
            DEFAULT_DEPARTURE_ID,
            DEFAULT_ARRIVAL_ID,
            TOTAL_DURATION,
            TOTAL_DISTANCE,
            List.of(firstPlanItem) // 첫 번째 아이템만 포함
        );

        // 물류 배송 담당자 조회 결과
        List<DriverResponse> logisticsDrivers = List.of(
            new DriverResponse(100L, "logistics-driver", "U999", "010-9999-9999", "logistics@test.com")
        );
        when(driverClientService.findLogisticsDrivers()).thenReturn(logisticsDrivers);

        DeliveryRoute savedRoute = DeliveryRoute.create(
            DEFAULT_DELIVERY_ID_FIRST,
            100L, // 물류 담당자 ID
            DEFAULT_DEPARTURE_ID,
            midHubId,
            1,
            50.0,
            20L,
            50.0,
            20L,
            DeliveryStatus.IN_HUB_TRANSIT
        );
        when(deliveryRouteRepository.save(any(DeliveryRoute.class))).thenReturn(savedRoute);

        // when
        DeliveryRouteStatusCommand result = deliveryRouteService.createDeliveryRoute(
            deliveryCommand, planCommand);

        // then: 상태가 IN_HUB_TRANSIT이고, 첫 번째 PlanItem 정보를 사용했는지 검증
        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(DeliveryStatus.IN_HUB_TRANSIT);
        assertThat(result.deliveryRouteId()).isEqualTo(savedRoute.getDeliveryRouteId());

        verify(deliveryRouteRepository, times(1)).save(any(DeliveryRoute.class));
        verify(driverClientService, times(1)).findLogisticsDrivers(); // 물류 담당자 조회
    }

    @Test
    @DisplayName("DeliveryRoute 저장 실패 시 예외 발생")
    void createDeliveryRoute_저장실패_예외() {
        // given
        DeliveryRouteCommand deliveryCommand = new DeliveryRouteCommand(
            DEFAULT_DELIVERY_ID_FIRST,
            DEFAULT_DEPARTURE_ID,
            DEFAULT_ARRIVAL_ID,
            DEFAULT_VENDOR_DRIVER_ID
        );

        DeliveryRoutePlanCommand planCommand = new DeliveryRoutePlanCommand(
            ROUTE_PLAN_ID,
            DEFAULT_DEPARTURE_ID,
            DEFAULT_ARRIVAL_ID,
            TOTAL_DURATION,
            TOTAL_DISTANCE,
            new ArrayList<>()
        );

        when(deliveryRouteRepository.save(any(DeliveryRoute.class)))
            .thenThrow(new RuntimeException("저장 실패"));

        // when & then: 저장 시 예외 발생 - BusinessException으로 변환됨
        assertThatThrownBy(() -> deliveryRouteService.createDeliveryRoute(
            deliveryCommand, planCommand))
            .isInstanceOf(BusinessException.class)
            .satisfies(exception -> {
                BusinessException businessException = (BusinessException) exception;
                assertThat(businessException.getErrorCode()).isEqualTo(
                    DeliveryErrorCode.DELIVERY_ROUTE_CREATION_FAILED);
            });
    }

    @Test
    @DisplayName("물류 배송 담당자 조회 실패 시 예외 발생")
    void createDeliveryRoute_물류담당자조회실패_예외() {
        // given: RoutePlanItem이 있는 경우
        UUID midHubId = UUID.randomUUID();
        UUID routePlanItemId = UUID.randomUUID();

        DeliveryRouteCommand deliveryCommand = new DeliveryRouteCommand(
            DEFAULT_DELIVERY_ID_FIRST,
            DEFAULT_DEPARTURE_ID,
            DEFAULT_ARRIVAL_ID,
            DEFAULT_VENDOR_DRIVER_ID
        );

        DeliveryRoutePlanCommand.PlanItem firstPlanItem = new DeliveryRoutePlanCommand.PlanItem(
            routePlanItemId,
            ROUTE_PLAN_ID,
            DEFAULT_DEPARTURE_ID,
            midHubId,
            20L,
            50.0,
            1
        );

        DeliveryRoutePlanCommand planCommand = new DeliveryRoutePlanCommand(
            ROUTE_PLAN_ID,
            DEFAULT_DEPARTURE_ID,
            DEFAULT_ARRIVAL_ID,
            TOTAL_DURATION,
            TOTAL_DISTANCE,
            List.of(firstPlanItem)
        );

        // 물류 배송 담당자 조회 실패
        when(driverClientService.findLogisticsDrivers())
            .thenThrow(new BusinessException(DeliveryErrorCode.EXTERNAL_API_ERROR));

        // when & then: 예외 발생 검증
        assertThatThrownBy(() -> deliveryRouteService.createDeliveryRoute(
            deliveryCommand, planCommand))
            .isInstanceOf(BusinessException.class);

        verify(driverClientService, times(1)).findLogisticsDrivers();
        verify(deliveryRouteRepository, times(0)).save(any(DeliveryRoute.class));
    }
}

