package com.klp.delivery.delivery.infrastructure.repository;

import com.klp.delivery.delivery.domain.entity.DeliveryRoute;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRouteJpaRepository extends JpaRepository<DeliveryRoute, UUID> {

    Optional<DeliveryRoute> findByDeliveryId(UUID deliveryId);
}
