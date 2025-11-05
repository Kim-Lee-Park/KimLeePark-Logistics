package com.klp.hub.inventory.infrastructure.repository;

import com.klp.hub.inventory.domain.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByProductId(UUID productId);
}
