package com.klp.hub.hub.infrastructure.repository;

import com.klp.hub.hub.domain.model.Hub;
import com.klp.hub.hub.domain.repository.HubRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HubRepositoryImpl implements HubRepository {
    private final HubJpaRepository hubJpaRepository;

    @Override
    public Hub save(Hub hub) {
        return hubJpaRepository.save(hub);
    }

    @Override
    public Optional<Hub> getHubById(UUID hubId) {
        return hubJpaRepository.findById(hubId);
    }

    @Override
    public Page<Hub> getHubs(Pageable pageable) {
        return hubJpaRepository.findAll(pageable);
    }
}
