package com.klp.delivery.delivery.presentation.dto;

import com.klp.delivery.common.DeliveryStatus;
import java.util.UUID;

public record DeliveryDetailResponse(
    UUID deliveryId,
    UUID orderId,
    UUID departureId,
    UUID arrivalId,
    UUID receiverId,
    String receiverName,
    String address,
    String receiverSlackId,
    DeliveryStatus status
) {

  public static DeliveryDetailResponse from(com.klp.delivery.delivery.domain.Delivery delivery) {
    return new DeliveryDetailResponse(
        delivery.getDeliveryId(),
        delivery.getOrderId(),
        delivery.getDepartureId(),
        delivery.getArrivalId(),
        delivery.getReceiverId(),
        delivery.getReceiverName(),
        delivery.getAddress(),
        delivery.getReceiverSlackId(),
        delivery.getStatus()
    );
  }
}

