package com.klp.delivery.delivery.domain.event;

import com.klp.delivery.delivery.application.command.OrderToDeliveryCommand.OrderItemCommand;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DeliveryRouteCreateEvent(
    UUID deliveryId,
    UUID orderId,
    UUID departureId,
    String departureName,
    UUID arrivalId,
    String arrivalName,
    Long drvierId,
    String userDriverSlackId,
    String userAddress,
    // Notification 이벤트에 필요한 정보
    String ordererName,
    String ordererEmail,
    LocalDateTime orderTime,
    String requirements,
    List<OrderItemCommand> orderItems,
    String driverName,
    String driverEmail
) {

}
