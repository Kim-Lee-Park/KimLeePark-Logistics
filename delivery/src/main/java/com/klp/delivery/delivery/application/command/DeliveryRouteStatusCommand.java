package com.klp.delivery.delivery.application.command;

import com.klp.delivery.common.enums.CustomerDeliveryStatus;
import java.util.UUID;

public record DeliveryRouteStatusCommand(
    UUID deliveryRouteId,
    CustomerDeliveryStatus status
) {

}