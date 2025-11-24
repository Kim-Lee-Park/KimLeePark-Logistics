package com.klp.delivery.delivery.infrastructure.repository;

import com.klp.delivery.delivery.domain.entity.IdempotencyKey;
import com.klp.delivery.delivery.domain.repository.IdempotencyKeyRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class IdempotencyKeyRepositoryImpl implements IdempotencyKeyRepository {

    private final IdempotencyKeyJpaRepository idempotencyKeyJpaRepository;


    @Override
    public IdempotencyKey save(IdempotencyKey idempotencyKey) {
        return idempotencyKeyJpaRepository.save(idempotencyKey);
    }

    @Override
    public Optional<IdempotencyKey> findByIdempotencyKey(String key) {
        return idempotencyKeyJpaRepository.findByIdempotencyKey(key);
    }
}
