package com.klp.delivery.routeplan.domain.model;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.klp.common.exception.BusinessException;
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


}
