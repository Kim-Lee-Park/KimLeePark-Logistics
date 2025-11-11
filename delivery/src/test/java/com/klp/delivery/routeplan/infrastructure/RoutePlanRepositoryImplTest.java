package com.klp.delivery.routeplan.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.klp.delivery.global.config.AuditConfig;
import com.klp.delivery.routeplan.domain.model.RoutePlan;
import com.klp.delivery.routeplan.fixture.RoutePlanFixture;
import com.klp.delivery.routeplan.infrastructure.repository.RoutePlanJpaRepository;
import com.klp.delivery.routeplan.infrastructure.repository.RoutePlanRepositoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import({RoutePlanRepositoryImpl.class, AuditConfig.class})
public class RoutePlanRepositoryImplTest {
    @Autowired
    private RoutePlanRepositoryImpl routePlanRepository;

    @Test
    void repository가_null_아님을_검증() {
        assertThat(routePlanRepository).isNotNull();
    }

    @Test
    @DisplayName("경로 계획 저장")
    void save(){
        RoutePlan routePlan = RoutePlanFixture.createRoutePlan();

        routePlan=routePlanRepository.save(routePlan);

        assertThat(routePlan.getRoutePlanId()).isNotNull();
    }
}
