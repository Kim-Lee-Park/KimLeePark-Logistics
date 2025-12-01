package com.klp.delivery.delivery.domain.repository;

import com.klp.delivery.delivery.domain.entity.DeliveryRoute;
import java.util.List;
import java.util.UUID;

public interface DeliveryRouteRepository {

    DeliveryRoute save(DeliveryRoute deliveryRoute);

    List<DeliveryRoute> findByDeliveryId(UUID deliveryId);
}
