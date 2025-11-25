package com.klp.hub.inventory.infrastructure.repository;

import com.klp.hub.inventory.domain.InventoryIdempotency;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryIdempotencyJpaRepository extends
    JpaRepository<InventoryIdempotency, UUID> {

    Optional<InventoryIdempotency> findByIdempotencyKey(String idempotencyKey);
}
