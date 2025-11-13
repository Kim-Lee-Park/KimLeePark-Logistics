package com.klp.delivery.delivery.domain.repository;

import com.klp.delivery.delivery.domain.entity.Delivery;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryRepository {

  Delivery save(Delivery delivery);

  Optional<Delivery> findByDeliveryId(UUID deliveryId);

}
