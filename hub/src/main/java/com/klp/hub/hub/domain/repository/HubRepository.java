package com.klp.hub.hub.domain.repository;

import com.klp.hub.hub.domain.model.Hub;
import com.klp.hub.hub.domain.model.HubStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HubRepository {

    Hub save(Hub hub);

    boolean existsByName(String name);

    boolean existsByAddress(String address);

    Optional<Hub> getHubById(UUID hubId);

    Page<Hub> getHubs(Pageable pageable);

    List<UUID> getActiveHubIds();

    List<Hub> getHubsByIds(List<UUID> hubIds);

    List<Hub> findAllByStatus(HubStatus hubStatus);
}
