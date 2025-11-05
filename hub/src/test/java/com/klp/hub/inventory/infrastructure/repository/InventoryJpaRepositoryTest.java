package com.klp.hub.inventory.infrastructure.repository;

import com.klp.hub.inventory.domain.Inventory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class InventoryJpaRepositoryTest {

    @Autowired
    InventoryJpaRepository inventoryJpaRepository;

    @Autowired
    EntityManager entityManager;

    private UUID productId = UUID.randomUUID();

    @Test
    @DisplayName("상품 ID 를 통해 재고를 조회할 수 있다")
    void findByProductId() {
        Inventory inventory = new Inventory(productId, 10);
        entityManager.persist(inventory);
        entityManager.flush();

        Optional<Inventory> result = inventoryJpaRepository.findByProductId(productId);

        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("상품의 재고가 존재하지 않는다면 Optional.empty 를 반환한다")
    void notFoundInventoryByProductId() {
        Optional<Inventory> result = inventoryJpaRepository.findByProductId(productId);

        assertFalse(result.isPresent());
        assertTrue(result.isEmpty());
    }
}
