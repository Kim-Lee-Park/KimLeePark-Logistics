package com.klp.notification.ai.application.command;

import java.time.LocalDateTime;
import java.util.List;

public record GenerateMessageCommand(
    String departureHubManagerId,
    LocalDateTime orderTime,
    String productName,
    Integer quantity,
    String requirements,
    LocalDateTime deliveryDeadline,
    String departureHubName,
    List<String> transitHubNames,
    String destinationAddress,
    String workingHours
) {

}
