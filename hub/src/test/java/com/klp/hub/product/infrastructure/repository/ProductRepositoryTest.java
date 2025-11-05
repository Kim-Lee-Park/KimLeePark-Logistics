package com.klp.hub.product.infrastructure.repository;

import com.klp.hub.company.domain.Company;
import com.klp.hub.company.domain.CompanyType;
import com.klp.hub.config.QuerydslConfig;
import com.klp.hub.product.domain.Product;
import com.klp.hub.product.domain.repository.ProductRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import({
        QuerydslConfig.class,
        ProductRepositoryImpl.class
})
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JPAQueryFactory queryFactory;

    @Autowired
    private EntityManager entityManager;

    private UUID companyId = UUID.randomUUID();

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
        var page = 0;
        var size = 2;
        Company company = new Company(CompanyType.SUPPLIER, "업체명", "업체주소");
        entityManager.persist(company);
        entityManager.flush();
        var companyId = company.getId();
        Product productA = new Product(companyId, "상품A");
        Product productB = new Product(companyId, "상품B");
        Product productC = new Product(companyId, "상품C");
        entityManager.persist(productA);
        entityManager.persist(productB);
        entityManager.persist(productC);
        entityManager.flush();

        var pageable = PageRequest.of(page, size);
        var result = productRepository.findAllByPageable(pageable);

        assertEquals(2, result.getContent().size());
        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
        assertTrue(result.isFirst());
        assertTrue(result.hasNext());
    }
}
