package com.klp.delivery.delivery.application.service;


import com.klp.delivery.delivery.application.command.DeliveryCommand;
import com.klp.delivery.delivery.domain.Delivery;
import com.klp.delivery.delivery.domain.DeliveryRepository;
import com.klp.delivery.delivery.presentation.dto.DeliveryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class DeliveryService {

  private final DeliveryRepository deliveryRepository;

  public DeliveryResponse registerDelivery(DeliveryCommand command) {

    Delivery delivery = deliveryRepository.save(
        Delivery.create(command.vendorDriverId(), command.orderId(),
            command.departureId(), command.arrivalId(), command.receiverId(),
            command.receiverName(),
            command.address(), command.receiverSlackId()));

    return new DeliveryResponse(delivery.getDeliveryId());

  }
}
