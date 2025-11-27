package com.klp.notification.messaging.infrastructure.repository;

import com.klp.notification.messaging.domain.entity.SlackMessage;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlackMessageJpaRepository extends JpaRepository<SlackMessage, UUID> {

}
