package com.klp.hub.inventory.infrastructure.repository;

import com.klp.hub.inventory.domain.Inventory;
import com.klp.hub.inventory.domain.repository.InventoryRepository;
import com.klp.hub.inventory.domain.repository.exception.UniqueConstraintException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class InventoryRepositoryImpl implements InventoryRepository {
    private final InventoryJpaRepository inventoryJpaRepository;

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
}
