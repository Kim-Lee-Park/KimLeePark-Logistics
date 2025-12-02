package com.klp.delivery.delivery.domain.repository;

import com.klp.delivery.delivery.domain.entity.IdempotencyKey;
import java.util.Optional;

public interface IdempotencyKeyRepository {

    IdempotencyKey save(IdempotencyKey idempotencyKey);

    Optional<IdempotencyKey> findByIdempotencyKey(String key);

    void deleteByIdempotencyKey(String idempotencyKey);
}
