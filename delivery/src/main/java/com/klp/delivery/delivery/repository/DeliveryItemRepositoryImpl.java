package com.klp.delivery.delivery.repository;

import com.klp.delivery.delivery.domain.entity.DeliveryItem;
import com.klp.delivery.delivery.domain.repository.DeliveryItemRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DeliveryItemRepositoryImpl implements DeliveryItemRepository {

    private final DeliveryItemJpaRepository deliveryItemJpaRepository;


    @Override
    public List<DeliveryItem> saveAll(List<DeliveryItem> deliveryItem) {

        return deliveryItemJpaRepository.saveAll(deliveryItem);
    }



    @Override
    public List<DeliveryItem> findAllByDeliveryId(UUID deliveryId) {

        return deliveryItemJpaRepository.findAllByDelivery_DeliveryId(deliveryId);
    }
}
