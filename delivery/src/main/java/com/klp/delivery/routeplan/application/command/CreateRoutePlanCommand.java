package com.klp.delivery.routeplan.application.command;

import java.util.UUID;

public record CreateRoutePlanCommand(
    UUID departureId,
    UUID arrivalId
) {

}
