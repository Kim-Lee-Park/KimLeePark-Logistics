package com.klp.notification.ai.infrastructure.repository;

import com.klp.notification.ai.domain.entity.AI;
import com.klp.notification.ai.domain.repository.AIRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AIRepositoryImpl implements AIRepository {

    private final AIJpaRepository aiJpaRepository;

    @Override
    public AI save(AI ai) {
        return aiJpaRepository.save(ai);
    }
}
