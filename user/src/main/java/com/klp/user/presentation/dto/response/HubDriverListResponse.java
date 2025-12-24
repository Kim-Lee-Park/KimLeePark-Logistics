package com.klp.user.presentation.dto.response;

import java.util.List;
import java.util.UUID;

public record HubDriverListResponse(
    UUID hubId,
    List<DriverInfo> drivers
) {
    public static HubDriverListResponse of(UUID hubId, List<DriverInfo> drivers) {
        return new HubDriverListResponse(hubId, drivers);
    }
}
