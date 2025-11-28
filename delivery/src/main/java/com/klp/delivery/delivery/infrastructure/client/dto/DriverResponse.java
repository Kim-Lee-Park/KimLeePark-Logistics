package com.klp.delivery.delivery.infrastructure.client.dto;

public record DriverResponse(
    Long userId,
    String username,
    String slackId,
    String phone,
    String email
) {

}
