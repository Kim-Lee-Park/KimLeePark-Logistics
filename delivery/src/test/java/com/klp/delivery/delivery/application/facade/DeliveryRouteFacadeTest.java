package com.klp.delivery.delivery.application.facade;

import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_ARRIVAL_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_DEPARTURE_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_DELIVERY_ID_FIRST;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_VENDOR_DRIVER_ID;
import static com.klp.delivery.routeplan.fixture.RoutePlanFixture.ROUTE_PLAN_ID;
import static com.klp.delivery.routeplan.fixture.RoutePlanFixture.TOTAL_DISTANCE;
import static com.klp.delivery.routeplan.fixture.RoutePlanFixture.TOTAL_DURATION;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.klp.delivery.common.enums.DeliveryStatus;
import com.klp.delivery.delivery.MockTest;
import com.klp.delivery.delivery.application.command.DeliveryRouteCommand;
import com.klp.delivery.delivery.application.command.DeliveryRoutePlanCommand;
import com.klp.delivery.delivery.application.command.DeliveryRouteStatusCommand;
import com.klp.delivery.delivery.application.service.DeliveryRouteService;
import com.klp.delivery.delivery.application.service.DeliveryService;
import com.klp.delivery.routeplan.application.service.RoutePlanService;
import com.klp.delivery.routeplan.presentation.dto.response.GetRoutePlanDetailResponse;
import java.util.ArrayList;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("배송 경로 Facade 테스트")
class DeliveryRouteFacadeTest extends MockTest {

    @InjectMocks
    DeliveryRouteFacade deliveryRouteFacade;

    @Mock
    DeliveryRouteService deliveryRouteService;

    @Mock
    DeliveryService deliveryService;

    @Mock
    RoutePlanService routePlanService;

    @Test
    @DisplayName("배송 경로 생성 성공 - RoutePlanItem 없음 (직행)")
    void CreateDeliveryRoute_직행_성공() {
        // given
        DeliveryRouteCommand command = new DeliveryRouteCommand(
            DEFAULT_DELIVERY_ID_FIRST,
            DEFAULT_DEPARTURE_ID,
            DEFAULT_ARRIVAL_ID,
            DEFAULT_VENDOR_DRIVER_ID
        );

        // RoutePlan 조회 결과 (직행 경로 - planItems가 비어있음)
        GetRoutePlanDetailResponse routePlanResponse = new GetRoutePlanDetailResponse(
            ROUTE_PLAN_ID,
            DEFAULT_DEPARTURE_ID,
            DEFAULT_ARRIVAL_ID,
            TOTAL_DURATION,
            TOTAL_DISTANCE,
            new ArrayList<>(), // planItems가 비어있음
            "ACTIVE"
        );

        DeliveryRouteStatusCommand statusCommand = new DeliveryRouteStatusCommand(
            UUID.randomUUID(),
            DeliveryStatus.ARRIVED_AT_FINAL_HUB
        );

        when(routePlanService.getRoutePlan(DEFAULT_DEPARTURE_ID, DEFAULT_ARRIVAL_ID))
            .thenReturn(routePlanResponse);
        when(deliveryRouteService.createDeliveryRoute(any(DeliveryRouteCommand.class),
            any(DeliveryRoutePlanCommand.class)))
            .thenReturn(statusCommand);

        // when
        deliveryRouteFacade.CreateDeliveryRoute(command);

        // then: RoutePlan 조회, DeliveryRoute 생성, Delivery 업데이트가 순서대로 호출되었는지 검증
        verify(routePlanService, times(1)).getRoutePlan(DEFAULT_DEPARTURE_ID, DEFAULT_ARRIVAL_ID);
        verify(deliveryRouteService, times(1)).createDeliveryRoute(
            any(DeliveryRouteCommand.class),
            any(DeliveryRoutePlanCommand.class)
        );
        verify(deliveryService, times(1)).applyRouteCreation(
            eq(DEFAULT_DELIVERY_ID_FIRST),
            eq(ROUTE_PLAN_ID),
            eq(DeliveryStatus.ARRIVED_AT_FINAL_HUB)
        );
    }

    @Test
    @DisplayName("배송 경로 생성 성공 - RoutePlanItem 있음 (경유)")
    void CreateDeliveryRoute_경유_성공() {
        // given
        UUID midHubId = UUID.randomUUID();
        UUID routePlanItemId = UUID.randomUUID();

        DeliveryRouteCommand command = new DeliveryRouteCommand(
            DEFAULT_DELIVERY_ID_FIRST,
            DEFAULT_DEPARTURE_ID,
            DEFAULT_ARRIVAL_ID,
            DEFAULT_VENDOR_DRIVER_ID
        );

        // RoutePlan 조회 결과 (경유 경로 - planItems가 있음)
        GetRoutePlanDetailResponse.PlanItem planItem = new GetRoutePlanDetailResponse.PlanItem(
            routePlanItemId,
            ROUTE_PLAN_ID,
            DEFAULT_DEPARTURE_ID,
            midHubId,
            20L,
            50.0,
            1
        );

        GetRoutePlanDetailResponse routePlanResponse = new GetRoutePlanDetailResponse(
            ROUTE_PLAN_ID,
            DEFAULT_DEPARTURE_ID,
            DEFAULT_ARRIVAL_ID,
            TOTAL_DURATION,
            TOTAL_DISTANCE,
            java.util.List.of(planItem),
            "ACTIVE"
        );

        DeliveryRouteStatusCommand statusCommand = new DeliveryRouteStatusCommand(
            UUID.randomUUID(),
            DeliveryStatus.IN_HUB_TRANSIT
        );

        when(routePlanService.getRoutePlan(DEFAULT_DEPARTURE_ID, DEFAULT_ARRIVAL_ID))
            .thenReturn(routePlanResponse);
        when(deliveryRouteService.createDeliveryRoute(any(DeliveryRouteCommand.class),
            any(DeliveryRoutePlanCommand.class)))
            .thenReturn(statusCommand);

        // when
        deliveryRouteFacade.CreateDeliveryRoute(command);

        // then: RoutePlan 조회, DeliveryRoute 생성, Delivery 업데이트가 순서대로 호출되었는지 검증
        verify(routePlanService, times(1)).getRoutePlan(DEFAULT_DEPARTURE_ID, DEFAULT_ARRIVAL_ID);
        verify(deliveryRouteService, times(1)).createDeliveryRoute(
            any(DeliveryRouteCommand.class),
            any(DeliveryRoutePlanCommand.class)
        );
        verify(deliveryService, times(1)).applyRouteCreation(
            eq(DEFAULT_DELIVERY_ID_FIRST),
            eq(ROUTE_PLAN_ID),
            eq(DeliveryStatus.IN_HUB_TRANSIT)
        );
    }
}

