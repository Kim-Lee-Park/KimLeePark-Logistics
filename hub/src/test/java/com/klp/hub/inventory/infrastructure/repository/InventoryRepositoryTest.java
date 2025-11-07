package com.klp.hub.inventory.infrastructure.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.klp.hub.TestJpaConfig;
import com.klp.hub.inventory.domain.Inventory;
import com.klp.hub.inventory.domain.repository.InventoryRepository;
import com.klp.hub.inventory.domain.repository.dto.InventoryDeduct;
import com.klp.hub.inventory.domain.repository.exception.UniqueConstraintException;
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

@DataJpaTest
@ActiveProfiles("test")
@Import({
    InventoryRepositoryImpl.class,
    TestJpaConfig.class
})
class InventoryRepositoryTest {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private EntityManager entityManager;

    private UUID productId = UUID.randomUUID();

    private UUID hubId = UUID.randomUUID();

    @Test
    @DisplayName("상품 ID 를 통해 재고를 조회할 수 있다")
    void findByProductId() {
        Inventory inventory = new Inventory(productId, hubId, 10);
        entityManager.persist(inventory);
        entityManager.flush();

        Optional<Inventory> result = inventoryRepository.findByProductId(productId);

        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("상품의 재고가 존재하지 않는다면 Optional.empty 를 반환한다")
    void notFoundInventoryByProductId() {
        Optional<Inventory> result = inventoryRepository.findByProductId(productId);

        assertFalse(result.isPresent());
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("같은 상품과 허브 조합으로 재고를 생성하려면 예외가 발생한다")
    void throwDuplicateInventoryHub() {
        Inventory inventoryA = new Inventory(productId, hubId, 10);
        inventoryRepository.save(inventoryA);

        Inventory duplicatedInventory = new Inventory(productId, hubId, 10);

        assertThrows(UniqueConstraintException.class, () -> {
            inventoryRepository.save(duplicatedInventory);
        });
    }

    @Test
    @DisplayName("같은 멱등키를 또 생성시도할때 false 를 반환한다")
    void throwDuplicateIdempotencyKey() {
        String idempotencyKeyA = "idempotencyKey";
        inventoryRepository.tryAcquireIdempotencyKey(idempotencyKeyA);
        String duplicateIdempotencyKey = "idempotencyKey";

        boolean result = inventoryRepository.tryAcquireIdempotencyKey(duplicateIdempotencyKey);

        assertFalse(result);
    }

    @Test
    @DisplayName("처음 멱등키를 생성을 시도한다면 true 를 반환한다")
    void createIdempotencyKey() {
        String idempotencyKey = "idempotencyKey";

        boolean result = inventoryRepository.tryAcquireIdempotencyKey(idempotencyKey);

        assertTrue(result);
    }

    @Test
    @DisplayName("재고가 충분하다면 재고를 차감한다")
    void deduct() {
        int quantity = 10;
        inventoryRepository.save(new Inventory(productId, hubId, quantity));
        List<InventoryDeduct> inventoryDeducts = List.of(
            new InventoryDeduct(productId, hubId, quantity)
        );

        int updated = inventoryRepository.deductAll(inventoryDeducts);
        entityManager.flush();
        entityManager.clear();
        Inventory inventory = inventoryRepository.findByProductId(productId).orElseThrow();

        assertEquals(1, updated);
        assertEquals(0, inventory.getQuantity());
    }

    @Test
    @DisplayName("재고가 충분하지 않다면 재고는 차감되지 않는다")
    void insufficientStock() {
        int quantity = 10;
        inventoryRepository.save(new Inventory(productId, hubId, quantity));
        List<InventoryDeduct> insufficientDeducts = List.of(
            new InventoryDeduct(productId, hubId, 11)
        );

        int updated = inventoryRepository.deductAll(insufficientDeducts);
        entityManager.flush();
        entityManager.clear();
        Inventory inventory = inventoryRepository.findByProductId(productId).orElseThrow();

        assertEquals(0, updated);
        assertEquals(10, inventory.getQuantity());
    }

    @Test
    @DisplayName("서로 다른 재고에 대한 요청시 재고가 충분하다면 모두 성공한다")
    void deductAll() {
        UUID productIdA = UUID.randomUUID();
        UUID hubIdA = UUID.randomUUID();
        UUID productIdB = UUID.randomUUID();
        UUID hubIdB = UUID.randomUUID();
        inventoryRepository.save(new Inventory(productIdA, hubIdA, 10));
        inventoryRepository.save(new Inventory(productIdB, hubIdB, 20));
        List<InventoryDeduct> inventoryDeducts = List.of(
            new InventoryDeduct(productIdA, hubIdA, 10),
            new InventoryDeduct(productIdB, hubIdB, 20)
        );

        int updated = inventoryRepository.deductAll(inventoryDeducts);
        entityManager.flush();
        entityManager.clear();
        Inventory inventoryA = inventoryRepository.findByProductId(productIdA).orElseThrow();
        Inventory inventoryB = inventoryRepository.findByProductId(productIdB).orElseThrow();

        assertEquals(2, updated);
        assertEquals(0, inventoryA.getQuantity());
        assertEquals(0, inventoryB.getQuantity());
    }

    @Test
    @DisplayName("서로 다른 재고에 대한 요청시 일부 재고가 부족하다면 해당 재고는 차감에 실패한다")
    void deductPartialFail() {
        UUID productIdA = UUID.randomUUID();
        UUID hubIdA = UUID.randomUUID();
        UUID productIdB = UUID.randomUUID();
        UUID hubIdB = UUID.randomUUID();
        inventoryRepository.save(new Inventory(productIdA, hubIdA, 10));
        inventoryRepository.save(new Inventory(productIdB, hubIdB, 20));
        List<InventoryDeduct> inventoryDeducts = List.of(
            new InventoryDeduct(productIdA, hubIdA, 10),
            new InventoryDeduct(productIdB, hubIdB, 21) // 1개 더 차감
        );

        int updated = inventoryRepository.deductAll(inventoryDeducts);
        entityManager.flush();
        entityManager.clear();
        Inventory inventoryA = inventoryRepository.findByProductId(productIdA).orElseThrow();
        Inventory inventoryB = inventoryRepository.findByProductId(productIdB).orElseThrow();

        assertEquals(1, updated);
        assertEquals(0, inventoryA.getQuantity());
        assertEquals(20, inventoryB.getQuantity());
    }
}
