package com.klp.delivery.routeplan.infrastructure.dto;

import com.klp.delivery.routeplan.application.command.HubInfo;
import java.util.UUID;

public record HubResponse(
    UUID hubId,
    String name,
    Double latitude,
    Double longitude,
    String address,
    String status
) {
    public HubInfo toCommand(){
        return new HubInfo(
            hubId,
            name,
            latitude,
            longitude,
            address,
            status
        );
    }
}
