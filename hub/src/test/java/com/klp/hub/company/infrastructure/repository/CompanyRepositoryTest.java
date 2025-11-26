package com.klp.hub.company.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.klp.hub.TestJpaConfig;
import com.klp.hub.company.domain.Company;
import com.klp.hub.company.domain.CompanyType;
import com.klp.hub.company.domain.repository.CompanyRepository;
import com.klp.hub.global.config.QuerydslConfig;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@ActiveProfiles("test")
@Import({
    CompanyRepositoryImpl.class,
    TestJpaConfig.class,
    QuerydslConfig.class
})
@TestPropertySource(properties = {
    "spring.sql.init.mode=never"
})
class CompanyRepositoryTest {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private EntityManager entityManager;

    private UUID hubId = UUID.randomUUID();

    @Test
    @DisplayName("업체 ID 를 통해 업체를 조회할 수 있다")
    void findById() {
        Company company = new Company(hubId, CompanyType.SUPPLIER, "업체명", "업체주소");
        entityManager.persist(company);
        entityManager.flush();

        Optional<Company> result = companyRepository.findById(company.getId());

        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("해당 상품이 존재하지 않는다면 Optional.empty 를 반환한다")
    void notFoundProductById() {
        Optional<Company> result = companyRepository.findById(UUID.randomUUID());

        assertFalse(result.isPresent());
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("업체명을 통해 업체 목록을 조회할 수 있다")
    void findAllByName() {
        Company companyA = new Company(hubId, CompanyType.SUPPLIER, "업체A", "주소");
        Company companyB = new Company(hubId, CompanyType.SUPPLIER, "업체A", "주소");
        Company companyC = new Company(hubId, CompanyType.SUPPLIER, "업체B", "주소");
        entityManager.persist(companyA);
        entityManager.persist(companyB);
        entityManager.persist(companyC);
        entityManager.flush();
        entityManager.clear();

        List<Company> results = companyRepository.findAllByName("업체A");

        assertEquals(2, results.size());
        assertThat(results)
            .extracting(Company::getName)
            .containsExactlyInAnyOrder("업체A", "업체A");
    }

    @Test
    @DisplayName("업체명으로 조회했을 때 결과가 없다면 빈 리스트를 반환한다")
    void findAllByNameIsEmpty() {
        Company companyA = new Company(hubId, CompanyType.SUPPLIER, "업체A", "주소");
        entityManager.persist(companyA);
        entityManager.flush();
        entityManager.clear();

        List<Company> results = companyRepository.findAllByName("없는 업체명");

        assertTrue(results.isEmpty());
        assertEquals(0, results.size());
    }
}
