package com.klp.delivery.delivery.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record DeliveryNotificationEvent(
    String departureHubManagerId,
    UUID orderId,
    String ordererName,
    String ordererEmail,
    LocalDateTime orderTime,
    String productName,
    Integer quantity,
    String requirements,
    LocalDateTime deliveryDeadline,
    String departureHubName,
    String transitHubNames,
    String destinationAddress,
    String driverName,
    String driverEmail
) {
}

