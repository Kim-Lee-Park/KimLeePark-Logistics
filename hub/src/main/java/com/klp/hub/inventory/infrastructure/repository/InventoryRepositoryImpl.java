package com.klp.hub.inventory.infrastructure.repository;

import com.klp.hub.inventory.domain.Inventory;
import com.klp.hub.inventory.domain.InventoryIdempotency;
import com.klp.hub.inventory.domain.InventoryIdempotencyStatus;
import com.klp.hub.inventory.domain.repository.InventoryRepository;
import com.klp.hub.inventory.domain.repository.dto.InventoryDeduct;
import com.klp.hub.inventory.domain.repository.dto.InventoryReplenish;
import com.klp.hub.inventory.domain.repository.exception.UniqueConstraintException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InventoryRepositoryImpl implements InventoryRepository {

    private final InventoryJpaRepository inventoryJpaRepository;

    private final InventoryIdempotencyJpaRepository idempotencyJpaRepository;

    private final JdbcTemplate jdbcTemplate;

    private final static String INVENTORY_TABLE = "p_inventory";

    @Override
    public Optional<Inventory> findByProductId(UUID productId) {
        return inventoryJpaRepository.findByProductId(productId);
    }

    @Override
    public Optional<Inventory> findById(UUID inventoryId) {
        return inventoryJpaRepository.findById(inventoryId);
    }

    @Override
    public Inventory save(Inventory inventory) {
        try {
            return inventoryJpaRepository.saveAndFlush(inventory);
        } catch (DataIntegrityViolationException exception) {
            throw new UniqueConstraintException("이미 해당 재고가 존재합니다.");
        }
    }

    @Override
    public InventoryIdempotencyStatus acquireIdempotencyKey(String idempotencyKey) {
        InventoryIdempotency entity = idempotencyJpaRepository.save(
            new InventoryIdempotency(idempotencyKey)
        );
        return entity.getStatus();
    }

    @Override
    public void idempotencySuccess(String idempotencyKey) {
        InventoryIdempotency inventoryIdempotency = idempotencyJpaRepository.findByIdempotencyKey(
                idempotencyKey
            )
            .orElseThrow(() -> new UniqueConstraintException("멱등키가 유니크하지 않습니다"));
        inventoryIdempotency.success();
        idempotencyJpaRepository.save(inventoryIdempotency);
    }

    @Override
    public int deductAll(List<InventoryDeduct> inventoryDeducts) {
        String sql = String.format("""
            UPDATE %s
                SET quantity = quantity - ?
            WHERE product_id = ?
                AND hub_id     = ?
                AND quantity   >= ?
            """, INVENTORY_TABLE);

        List<Object[]> batchArgs = inventoryDeducts.stream()
            .map(line -> new Object[]{
                line.quantity(),
                line.productId(),
                line.hubId(),
                line.quantity()
            })
            .toList();

        int[] updateCount = jdbcTemplate.batchUpdate(sql, batchArgs);
        return IntStream.of(updateCount).sum();
    }

    @Override
    public int replenishAll(List<InventoryReplenish> inventoryReplenishes) {
        String sql = String.format("""
            UPDATE %s
               SET quantity = quantity + ?
            WHERE product_id = ?
                AND hub_id     = ?
            """, INVENTORY_TABLE);

        List<Object[]> batchArgs = inventoryReplenishes.stream()
            .map(line -> new Object[]{
                line.quantity(),
                line.productId(),
                line.hubId(),
            })
            .toList();

        int[] updateCount = jdbcTemplate.batchUpdate(sql, batchArgs);
        return IntStream.of(updateCount).sum();
    }

    @Override
    public Optional<Inventory> findByProductIdAndHubId(UUID productId, UUID hubId) {
        return inventoryJpaRepository.findByProductIdAndHubId(productId, hubId);
    }
}
