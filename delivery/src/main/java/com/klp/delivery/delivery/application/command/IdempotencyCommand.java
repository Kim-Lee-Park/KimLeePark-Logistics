package com.klp.delivery.delivery.application.command;

import java.util.UUID;

public record IdempotencyCommand(

    String idempotencyKey,

    UUID orderId

) {

}
