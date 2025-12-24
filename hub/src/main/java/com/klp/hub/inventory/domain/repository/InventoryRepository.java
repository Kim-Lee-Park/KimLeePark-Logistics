package com.klp.hub.inventory.domain.repository;

import com.klp.hub.inventory.domain.Inventory;
import com.klp.hub.inventory.domain.InventoryIdempotencyStatus;
import com.klp.hub.inventory.domain.repository.dto.InventoryDeduct;
import com.klp.hub.inventory.domain.repository.dto.InventoryReplenish;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository {

    Optional<Inventory> findByProductId(UUID productId);

    Optional<Inventory> findById(UUID inventoryId);

    Inventory save(Inventory inventory);

    InventoryIdempotencyStatus acquireIdempotencyKey(String idempotencyKey);

    void idempotencySuccess(String idempotencyKey);

    int deductAll(List<InventoryDeduct> inventoryDeducts);

    int replenishAll(List<InventoryReplenish> inventoryReplenishes);

    Optional<Inventory> findByProductIdAndHubId(UUID productId, UUID hubId);
}
