package com.klp.delivery.delivery.infrastructure.client.dto;

import java.util.List;
import java.util.UUID;

public record HubDriverListResponse(
    UUID hubId,
    List<DriverInfo> drivers
) {

}