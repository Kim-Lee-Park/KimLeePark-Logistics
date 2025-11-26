package com.klp.hub.hub.infrastructure.repository;

import com.klp.hub.hub.domain.model.Hub;
import com.klp.hub.hub.domain.model.HubStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HubJpaRepository extends JpaRepository<Hub, UUID> {

    boolean existsByName(String name);

    boolean existsByAddress(String address);

    List<Hub> findAllByStatus(HubStatus hubStatus);

    Optional<Hub> findByName(String hubName);
}
