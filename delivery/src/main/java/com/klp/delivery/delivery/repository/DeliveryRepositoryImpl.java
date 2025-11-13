package com.klp.delivery.delivery.repository;

import com.klp.delivery.delivery.domain.entity.Delivery;
import com.klp.delivery.delivery.domain.repository.DeliveryRepository;
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
  public Optional<Delivery> findByDeliveryId(UUID deliveryId) {
    return deliveryJpaRepository.findByDeliveryId(deliveryId);
  }
}
