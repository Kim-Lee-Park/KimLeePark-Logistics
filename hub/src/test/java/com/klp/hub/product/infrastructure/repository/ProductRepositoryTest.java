package com.klp.hub.product.infrastructure.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.klp.hub.TestJpaConfig;
import com.klp.hub.company.domain.Company;
import com.klp.hub.company.domain.CompanyType;
import com.klp.hub.global.config.QuerydslConfig;
import com.klp.hub.product.domain.Product;
import com.klp.hub.product.domain.repository.ProductRepository;
import com.klp.hub.product.presentation.dto.ProductsPageRowResponse;
import com.querydsl.jpa.impl.JPAQueryFactory;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@ActiveProfiles("test")
@Import({
    QuerydslConfig.class,
    ProductRepositoryImpl.class,
    TestJpaConfig.class
})
@TestPropertySource(properties = {
    "spring.sql.init.mode=never"
})
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JPAQueryFactory queryFactory;

    @Autowired
    private EntityManager entityManager;

    private UUID companyId = UUID.randomUUID();

    private UUID hubId = UUID.randomUUID();

    @Test
    @DisplayName("상품 ID 를 통해 상품을 조회할 수 있다")
    void findById() {
        Product product = new Product(companyId, "상품명");
        entityManager.persist(product);
        entityManager.flush();

        Optional<Product> result = productRepository.findById(product.getId());

        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("해당 상품 ID를 가진 상품이 없다면 Optional.empty 를 반환한다")
    void notFoundProductById() {
        Optional<Product> result = productRepository.findById(UUID.randomUUID());

        assertFalse(result.isPresent());
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("총 3개의 데이터에서 1페이지, 2개의 데이터를 조회할 수 있다")
    void findAllByPageable() {
        int page = 0;
        int size = 2;
        Company company = new Company(hubId, CompanyType.SUPPLIER, "업체명", "업체주소");
        entityManager.persist(company);
        entityManager.flush();
        UUID companyId = company.getId();
        Product productA = new Product(companyId, "상품A");
        Product productB = new Product(companyId, "상품B");
        Product productC = new Product(companyId, "상품C");
        entityManager.persist(productA);
        entityManager.persist(productB);
        entityManager.persist(productC);
        entityManager.flush();

        PageRequest pageable = PageRequest.of(page, size);
        Page<ProductsPageRowResponse> result = productRepository.findAllByPageable(pageable);

        assertEquals(2, result.getContent().size());
        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
        assertTrue(result.isFirst());
        assertTrue(result.hasNext());
    }
}
