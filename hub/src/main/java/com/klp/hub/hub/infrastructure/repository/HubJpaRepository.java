package com.klp.hub.hub.infrastructure.repository;

import com.klp.hub.hub.domain.model.Hub;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HubJpaRepository extends JpaRepository<Hub, UUID> {

}
