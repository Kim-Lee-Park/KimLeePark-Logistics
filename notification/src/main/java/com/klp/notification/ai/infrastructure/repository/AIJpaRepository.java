package com.klp.notification.ai.infrastructure.repository;

import com.klp.notification.ai.domain.entity.AI;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AIJpaRepository extends JpaRepository<AI, UUID> {

}
