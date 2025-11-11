package com.klp.delivery.delivery.application.command;

import com.klp.delivery.common.IdempotencyStatus;
import java.util.UUID;

public record IdempotencyCommand(

    String idempotencyKey,

    UUID orderId,

    IdempotencyStatus status

) {

}
