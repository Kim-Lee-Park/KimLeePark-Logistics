package com.klp.user.presentation.dto.response;

import java.util.List;

public record LogisticsDriverListResponse(
    List<DriverInfo> drivers
) {
    public static LogisticsDriverListResponse of(List<DriverInfo> drivers) {
        return new LogisticsDriverListResponse(drivers);
    }
}
