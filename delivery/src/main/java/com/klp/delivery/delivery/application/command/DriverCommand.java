package com.klp.delivery.delivery.application.command;

import com.klp.delivery.delivery.infrastructure.client.dto.DriverInfo;
import java.util.List;

public record DriverCommand(
    Long userId,
    String username,
    String slackId,
    String phone,
    String email
) {
    public static DriverCommand of(DriverInfo response) {
        return new DriverCommand(
            response.userId(),
            response.username(),
            response.slackId(),
            response.phone(),
            response.email()
        );
    }

    public static List<DriverCommand> from(List<DriverInfo> deliveries) {
        return deliveries.stream()
            .map(DriverCommand::of)
            .toList();
    }
}
