package com.klp.notification.ai.application.command;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record GenerateMessageCommand(
    String departureHubManagerId,
    UUID orderId,
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
    String workingHours
) {

}
