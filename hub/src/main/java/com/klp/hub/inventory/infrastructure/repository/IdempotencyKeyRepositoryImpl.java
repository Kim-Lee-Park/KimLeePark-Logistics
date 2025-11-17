package com.klp.hub.inventory.infrastructure.repository;

import com.klp.hub.inventory.domain.repository.IdempotencyKeyRepository;

public class IdempotencyKeyRepositoryImpl implements IdempotencyKeyRepository {

    @Override
    public boolean tryAcquireIdempotencyKey(String idempotencyKey) {
        return false;
    }
}
