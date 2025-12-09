package com.klp.notification.messaging.domain.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Delivery 서비스에서 발행하는 배송 생성 이벤트 (구독용)
 */
public record DeliveryCreatedEvent(
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
