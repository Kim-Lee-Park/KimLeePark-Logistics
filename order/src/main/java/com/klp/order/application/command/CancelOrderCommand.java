package com.klp.order.application.command;

import com.klp.order.domain.entity.cancel.CancelType;
import java.util.List;
import java.util.UUID;

public record CancelOrderCommand(
    UUID orderId,
    UUID userCouponId,
    String cancelReason,
    Long cancelledBy,
    CancelType cancelType,
    List<OrderItemCommand> orderIitems
) {

}
