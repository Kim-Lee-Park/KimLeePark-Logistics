package com.klp.order.common.event;

import com.klp.order.common.event.domain.EventStore;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventStoreRepository extends JpaRepository<EventStore, UUID> {

}
