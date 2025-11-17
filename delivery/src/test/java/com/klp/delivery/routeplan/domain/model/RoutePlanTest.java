package com.klp.delivery.routeplan.domain.model;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.klp.common.exception.BusinessException;
import com.klp.delivery.routeplan.domain.vo.RouteInfoVo;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class RoutePlanTest {

    @Test
    @DisplayName("RoutePlan 생성 성공")
    void create_success(){
        //given
        UUID departureId = UUID.randomUUID();
        UUID arrivalId = UUID.randomUUID();

        //when
        RoutePlan routePlan=RoutePlan.create(departureId,arrivalId,30L,100.);

        //then
        assertThat(routePlan.getDepartureId()).isEqualTo(departureId);
        assertThat(routePlan.getArrivalId()).isEqualTo(arrivalId);
        assertThat(routePlan.getTotalDurationMin()).isEqualTo(30L);
        assertThat(routePlan.getTotalDistanceKm()).isEqualTo(100.);
    }

    @Test
    @DisplayName("출발 허브 ID는 필수")
    void create_departureIdIsNull_fail() {
        assertThatThrownBy(() -> RoutePlan.create(null, UUID.randomUUID(), 120L, 50.5))
            .isInstanceOf(BusinessException.class)
            .hasMessage("출발 허브 ID는 필수입니다.");
    }

    @Test
    @DisplayName("도착 허브 ID는 필수")
    void create_arrivalIdIsNull_fail() {
        assertThatThrownBy(() -> RoutePlan.create(UUID.randomUUID(),null, 120L, 50.5))
            .isInstanceOf(BusinessException.class)
            .hasMessage("도착 허브 ID는 필수입니다.");
    }

    @Test
    @DisplayName("총 소요 시간은 음수가 될 수 없다.")
    void create_durationMinNegative_fail() {
        assertThatThrownBy(() -> RoutePlan.create(UUID.randomUUID(),UUID.randomUUID(), -1L, 50.5))
            .isInstanceOf(BusinessException.class)
            .hasMessage("총 소요 시간은 0보다 커야 합니다.");
    }

    @Test
    @DisplayName("총 이동 거리는 음수가 될 수 없다.")
    void create_distanceKmNegative_fail() {
        assertThatThrownBy(() -> RoutePlan.create(UUID.randomUUID(),UUID.randomUUID(), 1L, -50.5))
            .isInstanceOf(BusinessException.class)
            .hasMessage("총 이동 거리는 0보다 커야 합니다.");
    }

    @Test
    @DisplayName("경유 경로 계획 - 직행보다 경유 시간이 더 짧으면 경유 경로를 선택한다")
    void plan_choose_shorter_indirect_route() {
        // given
        UUID origin = UUID.randomUUID();
        UUID mid = UUID.randomUUID();
        UUID dest = UUID.randomUUID();

        RouteInfoVo directRoute = new RouteInfoVo(
            UUID.randomUUID(),
            origin,
            dest,
            200L,
            150.0
        );

        RouteInfoVo originToMid = new RouteInfoVo(
            UUID.randomUUID(),
            origin,
            mid,
            50L,
            40.0
        );
        RouteInfoVo midToDest = new RouteInfoVo(
            UUID.randomUUID(),
            mid,
            dest,
            60L,
            50.0
        );

        RouteInfoVo originToDummy = new RouteInfoVo(
            UUID.randomUUID(),
            origin,
            UUID.randomUUID(),
            500L,
            500.0
        );

        List<RouteInfoVo> routeInfos = List.of(
            directRoute,
            originToMid,
            midToDest,
            originToDummy
        );

        // when
        RoutePlan routePlan = RoutePlan.plan(origin, dest, directRoute, routeInfos);

        // then
        assertThat(routePlan.getDepartureId()).isEqualTo(origin);
        assertThat(routePlan.getArrivalId()).isEqualTo(dest);
        assertThat(routePlan.getTotalDurationMin()).isEqualTo(110L);
        assertThat(routePlan.getTotalDistanceKm()).isEqualTo(90.0);

        List<RoutePlanItem> items = routePlan.getRoutePlanItems();
        assertThat(items).hasSize(2);

        RoutePlanItem first = items.get(0);
        RoutePlanItem second = items.get(1);

        assertThat(first.getDepartureId()).isEqualTo(origin);
        assertThat(first.getArrivalId()).isEqualTo(mid);
        assertThat(first.getSequence()).isEqualTo(1);

        assertThat(second.getDepartureId()).isEqualTo(mid);
        assertThat(second.getArrivalId()).isEqualTo(dest);
        assertThat(second.getSequence()).isEqualTo(2);
    }
}
