package com.klp.hub.inventory.infrastructure.repository;

import com.klp.hub.inventory.domain.InventoryIdempotency;
import com.klp.hub.inventory.domain.repository.IdempotencyKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class IdempotencyKeyRepositoryImpl implements IdempotencyKeyRepository {

    private final InventoryIdempotencyJpaRepository idempotencyJpaRepository;

    @Override
    public boolean tryAcquireIdempotencyKey(String idempotencyKey) {
        try {
            idempotencyJpaRepository.saveAndFlush(new InventoryIdempotency(idempotencyKey));
            return true;
        } catch (DataIntegrityViolationException exception) {
            return false;
        }
    }
}
