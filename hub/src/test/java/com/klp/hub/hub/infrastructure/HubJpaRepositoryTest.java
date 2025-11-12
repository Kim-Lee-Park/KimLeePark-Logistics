package com.klp.hub.hub.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.klp.hub.hub.domain.model.Hub;
import com.klp.hub.hub.infrastructure.HubJpaRepositoryTest.JpaAuditingTestConfig;
import com.klp.hub.hub.infrastructure.repository.HubJpaRepository;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@DataJpaTest
@Import(JpaAuditingTestConfig.class)
public class HubJpaRepositoryTest {
    @TestConfiguration
    @EnableJpaAuditing
    static class JpaAuditingTestConfig {
        @Bean
        AuditorAware<Long> auditorAware() {
            return () -> Optional.of(0L);
        }
    }

    @Autowired
    private HubJpaRepository hubJpaRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("저장 성공")
    void save(){
        Hub hub=Hub.create("test",11.,11.,"testAddress");

        hubJpaRepository.save(hub);

        assertThat(hub.getHubId()).isNotNull();
        assertThat(hub.getAddress()).isEqualTo("testAddress");
    }

    @Test
    @DisplayName("허브ID로 조회 성공")
    void findByName(){
        //given
        Hub hub=Hub.create("test",11.,11.,"testAddress");
        hubJpaRepository.saveAndFlush(hub);

        entityManager.clear();
        Optional<Hub> result=hubJpaRepository.findById(hub.getHubId());

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("허브 이름 존재 여부")
    void existsByName(){
        //given
        Hub hub=Hub.create("test",11.,11.,"testAddress");
        hubJpaRepository.save(hub);

        Boolean result=hubJpaRepository.existsByName("test");

        assertThat(result).isTrue();
    }
}
