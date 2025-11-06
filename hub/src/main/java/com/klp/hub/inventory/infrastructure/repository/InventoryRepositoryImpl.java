package com.klp.hub.inventory.infrastructure.repository;

import com.klp.hub.inventory.domain.Inventory;
import com.klp.hub.inventory.domain.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
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
}
