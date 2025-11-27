package com.klp.hub.hub.infrastructure.repository;

import com.klp.hub.hub.domain.model.Hub;
import com.klp.hub.hub.domain.model.HubStatus;
import com.klp.hub.hub.domain.model.QHub;
import com.klp.hub.hub.domain.repository.HubRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
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
    private final JPAQueryFactory queryFactory;

    @Override
    public Hub save(Hub hub) {
        return hubJpaRepository.save(hub);
    }

    @Override
    public boolean existsByName(String name) {
        return hubJpaRepository.existsByName(name);
    }

    @Override
    public boolean existsByAddress(String address) {
        return hubJpaRepository.existsByAddress(address);
    }

    @Override
    public Optional<Hub> getHubById(UUID hubId) {
        return hubJpaRepository.findById(hubId);
    }

    @Override
    public Page<Hub> getHubs(Pageable pageable) {
        return hubJpaRepository.findAll(pageable);
    }

    @Override
    public List<UUID> getActiveHubIds() {
        QHub qHub = QHub.hub;

        return queryFactory
            .select(qHub.hubId)
            .from(qHub)
            .where(qHub.status.eq(HubStatus.ACTIVE))
            .fetch();
    }

    @Override
    public List<Hub> getHubsByIds(List<UUID> hubIds) {
        QHub qHub = QHub.hub;
        return queryFactory
            .selectFrom(qHub)
            .where(qHub.hubId.in(hubIds))
            .fetch();
    }

    @Override
    public List<Hub> findAllByStatus(HubStatus hubStatus) {
        return hubJpaRepository.findAllByStatus(hubStatus);
    }

    @Override
    public Optional<Hub> findByName(String hubName) {
        return hubJpaRepository.findByNameAndDeletedAtIsNull(hubName);
    }
}
