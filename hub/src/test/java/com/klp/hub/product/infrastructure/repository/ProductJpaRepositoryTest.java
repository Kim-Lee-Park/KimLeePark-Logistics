package com.klp.hub.product.infrastructure.repository;

import com.klp.hub.product.domain.Product;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class ProductJpaRepositoryTest {

    @Autowired
    ProductJpaRepository productJpaRepository;

    @Autowired
    EntityManager entityManager;

    private UUID companyId = UUID.randomUUID();

    @Test
    @DisplayName("상품 ID 를 통해 상품을 조회할 수 있다")
    void findById() {
        Product product = new Product(companyId, "상품명");
        entityManager.persist(product);
        entityManager.flush();

        Optional<Product> result = productJpaRepository.findById(product.getId());

        assertTrue(result.isPresent());
    }
    
    @Test
    @DisplayName("해당 상품 ID를 가진 상품이 없다면 Optional.empty 를 반환한다")
    void notFoundProductById() {
        Optional<Product> result = productJpaRepository.findById(UUID.randomUUID());

        assertFalse(result.isPresent());
        assertTrue(result.isEmpty());
    }
}
