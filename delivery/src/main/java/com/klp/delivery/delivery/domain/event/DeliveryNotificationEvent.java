package com.klp.delivery.delivery.domain.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DeliveryNotificationEvent(
    UUID deliveryId,
    UUID orderId,
    String driverSlackId,
    String ordererName,
    String ordererEmail,
    LocalDateTime orderTime,
    String productName,
    Integer quantity,
    String requirements,
    String departureHubName,
    List<String> transitHubNames,
    String destinationAddress,
    String driverName,
    String driverEmail,
    String workingHours,
    LocalDateTime occurredAt
) {
}

