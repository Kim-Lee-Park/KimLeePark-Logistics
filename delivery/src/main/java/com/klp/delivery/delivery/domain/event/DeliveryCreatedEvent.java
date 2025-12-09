package com.klp.delivery.delivery.domain.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DeliveryCreatedEvent(
    UUID orderId,
    String status,
    List<DeliveryItem> items,
    // 알림 정보 (선택적 - DELIVERY_NOTIFICATION 이벤트 처리 시 포함)
    UUID deliveryId,
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

    public record DeliveryItem(
        UUID orderItemId,
        UUID deliveryId
    ) {

    }

}
