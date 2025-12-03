package com.klp.delivery.delivery.infrastructure.client.dto;

public record HubInfoResponse(
    String hubId,
    String name,
    Double latitude,
    Double longitude,
    String address,
    String status
) {
}