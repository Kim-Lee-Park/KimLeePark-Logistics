package com.klp.hub.inventory.domain.repository;

import com.klp.hub.inventory.domain.Inventory;

import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository {
    Optional<Inventory> findByProductId(UUID productId);
}
