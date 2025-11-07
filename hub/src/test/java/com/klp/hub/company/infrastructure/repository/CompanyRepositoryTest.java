package com.klp.hub.company.infrastructure.repository;

import com.klp.hub.TestJpaConfig;
import com.klp.hub.company.domain.Company;
import com.klp.hub.company.domain.CompanyType;
import com.klp.hub.company.domain.repository.CompanyRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
@Import({
        CompanyRepositoryImpl.class,
        TestJpaConfig.class
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
}
