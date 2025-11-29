package com.klp.delivery.delivery.application.command;

import com.klp.delivery.common.enums.DeliveryStatus;
import java.util.UUID;

public record DeliveryRouteStatusCommand(
    UUID deliveryRouteId,
    DeliveryStatus status
) {

}