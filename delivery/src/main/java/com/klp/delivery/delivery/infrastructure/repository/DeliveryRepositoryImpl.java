package com.klp.delivery.delivery.infrastructure.repository;

import com.klp.common.exception.BusinessException;
import com.klp.delivery.delivery.domain.entity.Delivery;
import com.klp.delivery.delivery.domain.repository.DeliveryRepository;
import com.klp.delivery.delivery.exception.DeliveryErrorCode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DeliveryRepositoryImpl implements DeliveryRepository {

    private final DeliveryJpaRepository deliveryJpaRepository;


    @Override
    public Delivery save(Delivery delivery) {
        return deliveryJpaRepository.save(delivery);
    }

    @Override
    public Delivery findByDeliveryId(UUID deliveryId) {
        return deliveryJpaRepository.findByDeliveryId(deliveryId)
            .orElseThrow(() -> new BusinessException(DeliveryErrorCode.DELIVERY_NOT_FOUND));
    }

    @Override
    public List<Delivery> findDeliveryByOrderId(UUID orderId) {
        return deliveryJpaRepository.findByOrderId(orderId);
    }
}
