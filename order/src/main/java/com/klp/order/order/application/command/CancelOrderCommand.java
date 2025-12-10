package com.klp.order.order.application.command;

import com.klp.order.order.domain.entity.cancel.CancelType;

public record CancelOrderCommand(
    String cancelReason,
    Long cancelledBy,
    CancelType cancelType
) {

}
