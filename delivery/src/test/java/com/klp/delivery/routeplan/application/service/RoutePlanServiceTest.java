package com.klp.delivery.routeplan.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.klp.common.exception.BusinessException;
import com.klp.delivery.routeplan.application.command.CreateRoutePlanCommand;
import com.klp.delivery.routeplan.application.command.HubInfo;
import com.klp.delivery.routeplan.application.command.HubRouteInfo;
import com.klp.delivery.routeplan.domain.model.RoutePlan;
import com.klp.delivery.routeplan.domain.policy.RoutePlanPolicy;
import com.klp.delivery.routeplan.domain.repository.RoutePlanRepository;
import com.klp.delivery.routeplan.domain.vo.RouteInfoVo;
import com.klp.delivery.routeplan.exception.RoutePlanErrorCode;
import com.klp.delivery.routeplan.fixture.HubFixture;
import com.klp.delivery.routeplan.fixture.RoutePlanFixture;
import com.klp.delivery.routeplan.presentation.dto.response.CreateRoutePlanResponse;
import com.klp.delivery.routeplan.presentation.dto.response.GetRoutePlanDetailResponse;
import com.klp.delivery.routeplan.presentation.dto.response.GetRoutePlanListResponse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class RoutePlanServiceTest {
    @Mock
    private RoutePlanRepository routePlanRepository;
    @Mock
    private HubRouteInfoClientService routeInfoClientService;
    @Mock
    private HubClientService hubClientService;
    @Mock
    private RoutePlanPolicy routePlanPolicy;

    @InjectMocks
    private RoutePlanService routePlanService;

    @Test
    @DisplayName("출발,도착 ID를 가진 경로 계획 존재하면 Business 예외")
    void createRoutePlan_alreadyExists_fail() {
        // given
        UUID depId = UUID.randomUUID();
        UUID arr = UUID.randomUUID();
        CreateRoutePlanCommand request =  new CreateRoutePlanCommand(depId, arr);
        given(routePlanRepository.existsByDepartureIdAndArrivalId(depId, arr)).willReturn(true);
        // when

        // then
        assertThatThrownBy(()->routePlanService.createRoutePlan(request))
            .isInstanceOf(BusinessException.class)
            .hasMessage(RoutePlanErrorCode.ALREADY_EXISTS_ROUTE_PLAN.getMessage());
    }

    @Test
    @DisplayName("출발 허브 미존재 시 BusinessException")
    void createRoutePlan_departureNotFound_fail() {
        // given
        UUID dep = UUID.randomUUID();
        UUID arr = UUID.randomUUID();
        CreateRoutePlanCommand request = new CreateRoutePlanCommand(dep,arr);
        given(routePlanRepository.existsByDepartureIdAndArrivalId(dep, arr)).willReturn(false);
        given(hubClientService.getHubById(dep))
            .willReturn(null);

        // when & then
        assertThatThrownBy(() -> routePlanService.createRoutePlan(request))
            .isInstanceOf(BusinessException.class)
            .hasMessage(RoutePlanErrorCode.HUB_NOT_FOUND.getMessage());

        verify(hubClientService, never()).getHubById(arr);
    }

    @Test
    @DisplayName("도착 허브 미존재 시 BusinessException")
    void createRoutePlan_arrivalNotFound_fail() {
        // given
        UUID dep = UUID.randomUUID();
        UUID arr = UUID.randomUUID();
        CreateRoutePlanCommand request = new CreateRoutePlanCommand(dep,arr);
        HubInfo departureHub=HubFixture.createHubWithId(dep);
        given(hubClientService.getHubById(dep)).willReturn(departureHub);

        // when & then
        assertThatThrownBy(() -> routePlanService.createRoutePlan(request))
            .isInstanceOf(BusinessException.class)
            .hasMessage(RoutePlanErrorCode.HUB_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("출발-도착 이동 정보 미존재 시 BusinessException")
    void createRoutePlan_hubRouteInfoNotFound_fail() {
        // given
        UUID dep = UUID.randomUUID();
        UUID arr = UUID.randomUUID();
        CreateRoutePlanCommand request = new CreateRoutePlanCommand(dep,arr);
        HubInfo departureHub=HubFixture.createHubWithId(dep);
        HubInfo arrivalHub=HubFixture.createHubWithId(arr);
        given(routePlanRepository.existsByDepartureIdAndArrivalId(dep, arr)).willReturn(false);
        given(hubClientService.getHubById(dep)).willReturn(departureHub);
        given(hubClientService.getHubById(arr)).willReturn(arrivalHub);

        // when & then
        assertThatThrownBy(() -> routePlanService.createRoutePlan(request))
            .isInstanceOf(BusinessException.class)
            .hasMessage(RoutePlanErrorCode.HUB_ROUTE_INFO_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("경로 계획 생성 직행 경로 성공")
    void createRoutePlan_direct_success() {
        // given
        UUID dep = UUID.randomUUID();
        UUID arr = UUID.randomUUID();
        UUID routeInfoId = UUID.randomUUID();
        UUID routePlanId = UUID.randomUUID();

        CreateRoutePlanCommand command = new CreateRoutePlanCommand(dep, arr);

        HubInfo departureHub = HubFixture.createHubWithId(dep);
        HubInfo arrivalHub = HubFixture.createHubWithId(arr);
        HubRouteInfo hubRouteInfo = new HubRouteInfo(routeInfoId, dep, arr, 1L, 10.0);

        given(routePlanRepository.existsByDepartureIdAndArrivalId(dep, arr))
            .willReturn(false);
        given(hubClientService.getHubById(dep)).willReturn(departureHub);
        given(hubClientService.getHubById(arr)).willReturn(arrivalHub);
        given(routeInfoClientService.getHubRouteInfo(dep, arr))
            .willReturn(hubRouteInfo);
        given(routePlanPolicy.isDirectAllowed(hubRouteInfo.distanceKm()))
            .willReturn(true);

        RoutePlan savedRoutePlan =
            RoutePlan.create(dep, arr, hubRouteInfo.durationMin(), hubRouteInfo.distanceKm());
        ReflectionTestUtils.setField(savedRoutePlan, "routePlanId", routePlanId);

        given(routePlanRepository.save(any(RoutePlan.class)))
            .willReturn(savedRoutePlan);

        // when
        CreateRoutePlanResponse response = routePlanService.createRoutePlan(command);

        // then
        assertThat(response).isNotNull();
        assertThat(response.routePlanId()).isEqualTo(routePlanId);

        verify(routePlanRepository).existsByDepartureIdAndArrivalId(dep, arr);
        verify(hubClientService).getHubById(dep);
        verify(hubClientService).getHubById(arr);
        verify(routeInfoClientService).getHubRouteInfo(dep, arr);
        verify(routePlanPolicy).isDirectAllowed(hubRouteInfo.distanceKm());
        verify(routePlanRepository).save(any(RoutePlan.class));
    }

    @Test
    @DisplayName("경로 계획 생성 경유 경로 성공")
    void createRoutePlan_via_success() {
        // given
        UUID dep = UUID.randomUUID();
        UUID arr = UUID.randomUUID();
        UUID routeInfoId = UUID.randomUUID();
        UUID routePlanId = UUID.randomUUID();

        CreateRoutePlanCommand command = new CreateRoutePlanCommand(dep, arr);

        HubInfo departureHub = HubFixture.createHubWithId(dep);
        HubInfo arrivalHub = HubFixture.createHubWithId(arr);

        // 직행 허브간 이동 정보 (정책상 직행 불가라서 plan()에서는 directRoute 로만 쓰임)
        HubRouteInfo directHubRoute = new HubRouteInfo(routeInfoId, dep, arr, 200L, 150.0);
        RouteInfoVo directRouteVo = directHubRoute.toVo(); // plan() 의 directRoute 인자

        UUID mid = UUID.randomUUID();

        // 경유 후보들 (HubRouteInfo)
        HubRouteInfo originToMidHub = new HubRouteInfo(
            UUID.randomUUID(),
            dep,
            mid,
            50L,
            40.0
        );
        HubRouteInfo midToDestHub = new HubRouteInfo(
            UUID.randomUUID(),
            mid,
            arr,
            60L,
            50.0
        );
        HubRouteInfo originToDummyHub = new HubRouteInfo(
            UUID.randomUUID(),
            dep,
            UUID.randomUUID(),
            500L,
            500.0
        );

        // 서비스에서 사용하는 전체 허브간 이동 정보 리스트
        List<HubRouteInfo> allHubRouteInfos = List.of(
            directHubRoute,
            originToMidHub,
            midToDestHub,
            originToDummyHub
        );

        // RoutePlan.plan 에 넘길 VO 리스트 (도메인 로직과 동일하게 변환)
        List<RouteInfoVo> routeInfoVos = allHubRouteInfos.stream()
            .map(HubRouteInfo::toVo)
            .toList();

        given(routePlanRepository.existsByDepartureIdAndArrivalId(dep, arr))
            .willReturn(false);

        given(hubClientService.getHubById(dep)).willReturn(departureHub);
        given(hubClientService.getHubById(arr)).willReturn(arrivalHub);

        given(routeInfoClientService.getHubRouteInfo(dep, arr))
            .willReturn(directHubRoute);

        given(routePlanPolicy.isDirectAllowed(directHubRoute.distanceKm()))
            .willReturn(false);

        given(routeInfoClientService.getHubRouteInfos())
            .willReturn(allHubRouteInfos);

        RoutePlan savedRoutePlan =
            RoutePlan.plan(dep, arr, directRouteVo, routeInfoVos);
        ReflectionTestUtils.setField(savedRoutePlan, "routePlanId", routePlanId);

        given(routePlanRepository.save(any(RoutePlan.class)))
            .willReturn(savedRoutePlan);

        // when
        CreateRoutePlanResponse response = routePlanService.createRoutePlan(command);

        // then
        assertThat(response).isNotNull();
        assertThat(response.routePlanId()).isEqualTo(routePlanId);

        verify(routePlanRepository).existsByDepartureIdAndArrivalId(dep, arr);
        verify(hubClientService).getHubById(dep);
        verify(hubClientService).getHubById(arr);
        verify(routeInfoClientService).getHubRouteInfo(dep, arr);
        verify(routePlanPolicy).isDirectAllowed(directHubRoute.distanceKm());
        verify(routeInfoClientService).getHubRouteInfos();
        verify(routePlanRepository).save(any(RoutePlan.class));
    }

    @Test
    @DisplayName("출발 허브ID, 도착허브ID로 조회")
    void get_byDepartureIdAndArrivalId_success() {
        // given
        UUID depId=RoutePlanFixture.DEPARTURE_ID;
        UUID arrId=RoutePlanFixture.ARRIVAL_ID;
        RoutePlan routePlan = RoutePlanFixture.createRoutePlan();
        given(routePlanRepository.findByDepartureIdAndArrivalIdAndDeletedAtIsNull(depId, arrId)).willReturn(Optional.of(routePlan));
        // when
        GetRoutePlanDetailResponse response=routePlanService.getRoutePlan(depId,arrId);
        // then
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("경로 계획 ID로 조회")
    void get_byRoutePlanId_success() {
        // given
        UUID routePlanId=RoutePlanFixture.ROUTE_PLAN_ID;
        RoutePlan routePlan = RoutePlanFixture.createRoutePlan();
        given(routePlanRepository.findByRoutePlanIdAndDeletedAtIsNull(routePlanId)).willReturn(Optional.of(routePlan));
        // when
        GetRoutePlanDetailResponse response = routePlanService.getRoutePlan(routePlanId);
        // then
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("경로 계획 목록 조회")
    void get_All_success() {
        // given
        UUID depId=RoutePlanFixture.DEPARTURE_ID;
        UUID arrId=RoutePlanFixture.ARRIVAL_ID;
        Pageable pageable= PageRequest.of(0, 10);
        RoutePlan routePlan = RoutePlanFixture.createRoutePlan();
        given(routePlanRepository.findAll(depId,arrId,pageable)).willReturn(new PageImpl<>(List.of(routePlan),pageable,1));
        // when
        GetRoutePlanListResponse response = routePlanService.getRoutePlans(depId,arrId,pageable);
        // then
        assertThat(response.routePlans()).hasSize(1);
        assertThat(response.pageable().totalElements()).isEqualTo(1);
    }
}
