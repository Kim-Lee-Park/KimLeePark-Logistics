package com.klp.delivery.routeplan.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.klp.delivery.global.config.AuditConfig;
import com.klp.delivery.global.config.QuerydslConfig;
import com.klp.delivery.routeplan.domain.model.RoutePlan;
import com.klp.delivery.routeplan.domain.repository.RoutePlanRepository;
import com.klp.delivery.routeplan.fixture.RoutePlanFixture;
import com.klp.delivery.routeplan.infrastructure.repository.RoutePlanRepositoryImpl;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import({RoutePlanRepositoryImpl.class, AuditConfig.class, QuerydslConfig.class})
public class RoutePlanRepositoryImplTest {
    @Autowired
    private RoutePlanRepository routePlanRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    void repository가_null_아님을_검증() {
        assertThat(routePlanRepository).isNotNull();
    }

    @Test
    @DisplayName("경로 계획 저장")
    void save(){
        //given
        RoutePlan routePlan = RoutePlanFixture.createRoutePlan();

        //when
        routePlan=routePlanRepository.save(routePlan);
        entityManager.flush();

        //then
        assertThat(routePlan.getRoutePlanId()).isNotNull();
    }

    @Test
    @DisplayName("출발 허브ID,도착 허브ID로 경로 계획 조회")
    void find_byDepartureIdAndArrivalId_success(){
        //given
        RoutePlan routePlan = RoutePlanFixture.createRoutePlan();
        routePlan=routePlanRepository.save(routePlan);
        entityManager.flush();

        //when
        Optional<RoutePlan> foundRoutePlan=routePlanRepository.findByDepartureIdAndArrivalIdAndDeletedAtIsNull(routePlan.getDepartureId(),routePlan.getArrivalId());

        //then
        assertThat(foundRoutePlan).isPresent();
    }

    @Test
    @DisplayName("출발 허브 ID, 도착 허브 ID 해당하는 경로 계획 삭제")
    void delete_whereDepartureIdAndArrivalId_success(){
        //given
        RoutePlan routePlan = RoutePlanFixture.createRoutePlan();
        routePlanRepository.save(routePlan);
        entityManager.flush();

        //when
        routePlanRepository.softDeleteByDepartureAndArrival(routePlan.getDepartureId(), routePlan.getArrivalId());
        entityManager.clear();

        //then
        Optional<RoutePlan> found = routePlanRepository.findByDepartureIdAndArrivalIdAndDeletedAtIsNull(
            routePlan.getDepartureId(), routePlan.getArrivalId());
        assertThat(found).isPresent();
        assertThat(found.get().getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("출발 허브 ID, 도착 허브 ID을 가진 경로 계획 존재 여부 조회")
    void exists_whereDepartureIdAndArrivalId_success() {
        // given
        RoutePlan routePlan = RoutePlanFixture.createRoutePlan();
        routePlanRepository.save(routePlan);
        entityManager.flush();

        // when
        boolean result=routePlanRepository.existsByDepartureIdAndArrivalId(routePlan.getDepartureId(), routePlan.getArrivalId());
        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("경로 계획 ID로 조회")
    void find_byRouteInfoId_success() {
        // given
        RoutePlan routePlan = RoutePlanFixture.createRoutePlan();
        routePlanRepository.save(routePlan);
        entityManager.flush();
        // when
        Optional<RoutePlan> found=routePlanRepository.getRouteInfoById(routePlan.getRoutePlanId());
        // then
        assertThat(found).isPresent();
    }

    @Test
    @DisplayName("경로 계획 목록 조회")
    void findAll_success() {
        // given
        RoutePlan routePlan = RoutePlanFixture.createRoutePlan();
        routePlanRepository.save(routePlan);
        entityManager.flush();
        Pageable pageable= PageRequest.of(0,10);
        // when
        Page<RoutePlan> routePlans = routePlanRepository.findAll(null,null,pageable);
        // then
        assertThat(routePlans).isNotNull();
        assertThat(routePlans.get().count()).isEqualTo(1);
    }
}
