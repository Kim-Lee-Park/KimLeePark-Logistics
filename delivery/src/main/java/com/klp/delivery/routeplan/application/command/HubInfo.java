package com.klp.delivery.routeplan.application.command;

import java.util.UUID;

public record HubInfo(
    UUID hubId,
    String name,
    Double latitude,
    Double longitude,
    String address,
    String status
) {

}
