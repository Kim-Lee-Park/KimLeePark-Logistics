package com.klp.delivery.delivery.infrastructure.client.dto;

import java.util.List;

public record LogisticsDriverListResponse(
    List<DriverInfo> drivers
) {

}
