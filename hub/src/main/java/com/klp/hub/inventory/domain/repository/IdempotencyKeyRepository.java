package com.klp.hub.inventory.domain.repository;

public interface IdempotencyKeyRepository {

    boolean tryAcquireIdempotencyKey(String idempotencyKey);
}
