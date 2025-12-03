package com.klp.delivery.delivery.infrastructure.repository;

import com.klp.delivery.delivery.domain.entity.DeliveryRoute;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRouteJpaRepository extends JpaRepository<DeliveryRoute, UUID> {

    List<DeliveryRoute> findByDeliveryId(UUID routePlanId);
}
