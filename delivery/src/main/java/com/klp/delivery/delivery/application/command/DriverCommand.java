package com.klp.delivery.delivery.application.command;

public record DriverCommand(
    Long vendorDrvierId,
    String receiverSlackId
) {

}
