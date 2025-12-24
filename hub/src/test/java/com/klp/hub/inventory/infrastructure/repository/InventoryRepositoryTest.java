package com.klp.hub.inventory.infrastructure.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.klp.hub.TestJpaConfig;
import com.klp.hub.inventory.domain.Inventory;
import com.klp.hub.inventory.domain.repository.InventoryRepository;
import com.klp.hub.inventory.domain.repository.dto.InventoryDeduct;
import com.klp.hub.inventory.domain.repository.dto.InventoryReplenish;
import com.klp.hub.inventory.domain.repository.exception.UniqueConstraintException;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@Disabled
@DataJpaTest
@ActiveProfiles("test")
@Import({
    InventoryRepositoryImpl.class,
    TestJpaConfig.class
})
@TestPropertySource(properties = {
    "spring.sql.init.mode=never"
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
    @DisplayName("재고가 없는 상품에 대해서 재고 차감시 재고는 차감되지 않는다")
    void deductNonExistentInventory() {
        int addQuantity = 10;
        List<InventoryDeduct> inventoryDeducts = List.of(
            new InventoryDeduct(productId, hubId, addQuantity)
        );

        int updated = inventoryRepository.deductAll(inventoryDeducts);
        entityManager.flush();
        entityManager.clear();

        assertEquals(0, updated);
    }

    @Test
    @DisplayName("서로 다른 재고에 대한 차감시 특정 재고가 존재하지 않는다면 해당 재고는 차감에 실패한다")
    void deductAllNonExistentInventory() {
        UUID nonExistentProductId = UUID.randomUUID();
        UUID nonExistentHubId = UUID.randomUUID();
        UUID productIdB = UUID.randomUUID();
        UUID hubIdB = UUID.randomUUID();
        inventoryRepository.save(new Inventory(productIdB, hubIdB, 20));
        List<InventoryDeduct> inventoryDeducts = List.of(
            new InventoryDeduct(nonExistentProductId, nonExistentHubId, 10),
            new InventoryDeduct(productIdB, hubIdB, 20)
        );

        int updated = inventoryRepository.deductAll(inventoryDeducts);
        entityManager.flush();
        entityManager.clear();

        assertEquals(1, updated);
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
    @DisplayName("서로 다른 재고에 대한 차감시 재고가 충분하다면 모두 차감된다")
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
    @DisplayName("서로 다른 재고에 대한 차감시 일부 재고가 부족하다면 해당 재고는 차감에 실패한다")
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

    @Test
    @DisplayName("재고가 있는 상품의 재고를 증가시킨다")
    void replenish() {
        int quantity = 10;
        int addQuantity = 10;
        inventoryRepository.save(new Inventory(productId, hubId, quantity));
        List<InventoryReplenish> inventoryReplenishes = List.of(
            new InventoryReplenish(productId, hubId, addQuantity)
        );

        int updated = inventoryRepository.replenishAll(inventoryReplenishes);
        entityManager.flush();
        entityManager.clear();
        Inventory inventory = inventoryRepository.findByProductId(productId).orElseThrow();

        assertEquals(1, updated);
        assertEquals(quantity + addQuantity, inventory.getQuantity());
    }

    @Test
    @DisplayName("서로 다른 재고에 대한 증가시 모두 성공한다")
    void addAll() {
        UUID productIdA = UUID.randomUUID();
        UUID hubIdA = UUID.randomUUID();
        UUID productIdB = UUID.randomUUID();
        UUID hubIdB = UUID.randomUUID();
        inventoryRepository.save(new Inventory(productIdA, hubIdA, 10));
        inventoryRepository.save(new Inventory(productIdB, hubIdB, 20));
        List<InventoryReplenish> inventoryReplenishes = List.of(
            new InventoryReplenish(productIdA, hubIdA, 10),
            new InventoryReplenish(productIdB, hubIdB, 20)
        );

        int updated = inventoryRepository.replenishAll(inventoryReplenishes);
        entityManager.flush();
        entityManager.clear();
        Inventory inventoryA = inventoryRepository.findByProductId(productIdA).orElseThrow();
        Inventory inventoryB = inventoryRepository.findByProductId(productIdB).orElseThrow();

        assertEquals(2, updated);
        assertEquals(20, inventoryA.getQuantity());
        assertEquals(40, inventoryB.getQuantity());
    }

    @Test
    @DisplayName("재고가 없는 상품에 대해서 재고 증가시 재고는 증가되지 않는다")
    void replenishNonExistentInventory() {
        int addQuantity = 10;
        List<InventoryReplenish> inventoryReplenishes = List.of(
            new InventoryReplenish(productId, hubId, addQuantity)
        );

        int updated = inventoryRepository.replenishAll(inventoryReplenishes);
        entityManager.flush();
        entityManager.clear();

        assertEquals(0, updated);
    }

    @Test
    @DisplayName("서로 다른 재고에 대한 증가시 특정 재고가 존재하지 않는다면 해당 재고는 증가에 실패한다")
    void replenishAllNonExistentInventory() {
        UUID nonExistentProductId = UUID.randomUUID();
        UUID nonExistentHubId = UUID.randomUUID();
        UUID productIdB = UUID.randomUUID();
        UUID hubIdB = UUID.randomUUID();
        Inventory savedInventory = inventoryRepository.save(new Inventory(productIdB, hubIdB, 20));
        List<InventoryReplenish> inventoryReplenishes = List.of(
            new InventoryReplenish(nonExistentProductId, nonExistentHubId, 10),
            new InventoryReplenish(productIdB, hubIdB, 20)
        );

        int updated = inventoryRepository.replenishAll(inventoryReplenishes);
        entityManager.flush();
        entityManager.clear();

        assertEquals(1, updated);
        assertEquals(20, savedInventory.getQuantity());
    }
}
