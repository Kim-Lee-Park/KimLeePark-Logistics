package com.klp.order.application.command;

import com.klp.order.domain.entity.cancel.CancelType;

public record CancelOrderCommand(
    String cancelReason,
    Long cancelledBy,
    CancelType cancelType
) {

}
