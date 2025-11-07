package com.klp.delivery.delivery.repository;

import com.klp.delivery.delivery.domain.IdempotencyKey;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyKeyJpaRepository extends JpaRepository<IdempotencyKey, String> {

  Optional<IdempotencyKey> findByIdempotencyKey(String key);
}
