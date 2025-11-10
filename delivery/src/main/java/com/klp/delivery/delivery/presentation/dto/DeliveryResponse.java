package com.klp.delivery.delivery.presentation.dto;

import java.util.List;
import java.util.UUID;

public record DeliveryResponse(
    List<DeliveryItemResponse> deliveries
) {
  public record DeliveryItemResponse(
      UUID orderItemId,
      UUID deliveryId
  ) {
  }
}
