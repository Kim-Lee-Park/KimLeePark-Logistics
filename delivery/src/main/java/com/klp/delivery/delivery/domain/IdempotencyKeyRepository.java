package com.klp.delivery.delivery.domain;

import java.util.Optional;

public interface IdempotencyKeyRepository {

  IdempotencyKey save(IdempotencyKey idempotencyKey);

  Optional<IdempotencyKey> findByIdempotencyKey(String key);

}
