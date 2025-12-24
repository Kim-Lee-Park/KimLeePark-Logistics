package com.klp.delivery.delivery.application.command;

import com.klp.delivery.routeplan.application.command.HubInfo;
import java.util.UUID;

public record HubInfoCommand(
    UUID hubId,
    String name,
    Double latitude,
    Double longitude,
    String address,
    String status
) {

    public static HubInfoCommand of(HubInfo response) {
        return new HubInfoCommand(
            response.hubId(),
            response.name(),
            response.latitude(),
            response.longitude(),
            response.address(),
            response.status()
        );
    }

}