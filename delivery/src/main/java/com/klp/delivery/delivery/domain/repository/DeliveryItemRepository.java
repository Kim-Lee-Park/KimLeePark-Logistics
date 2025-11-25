package com.klp.delivery.delivery.domain.repository;

import com.klp.delivery.delivery.domain.entity.DeliveryItem;
import java.util.List;
import java.util.UUID;

public interface DeliveryItemRepository {


    List<DeliveryItem> saveAll(List<DeliveryItem> deliveryItem);


    List<DeliveryItem> findAllByDeliveryId(UUID DeliveryId);

}
