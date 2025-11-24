package com.klp.delivery.delivery.infrastructure.repository;

import com.klp.delivery.delivery.domain.entity.DeliveryItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface DeliveryItemJpaRepository extends JpaRepository<DeliveryItem, UUID> {

    List<DeliveryItem> findAllByDelivery_DeliveryId(@Param("deliveryId") UUID deliveryId);
}
