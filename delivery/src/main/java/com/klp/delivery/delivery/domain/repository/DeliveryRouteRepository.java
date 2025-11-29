package com.klp.delivery.delivery.domain.repository;

import com.klp.delivery.delivery.domain.entity.DeliveryRoute;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryRouteRepository {

    DeliveryRoute save(DeliveryRoute deliveryRoute);

    Optional<DeliveryRoute> findByDeliveryId(UUID deliveryId);
}
