package com.klp.hub.inventory.domain.repository;

import com.klp.hub.inventory.domain.Inventory;
import com.klp.hub.inventory.domain.repository.dto.InventoryDeduct;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository {

    Optional<Inventory> findByProductId(UUID productId);

    Optional<Inventory> findById(UUID inventoryId);

    Inventory save(Inventory inventory);

    boolean tryAcquireIdempotencyKey(String idempotencyKey);

    int deductAll(List<InventoryDeduct> inventoryDeducts);
}
