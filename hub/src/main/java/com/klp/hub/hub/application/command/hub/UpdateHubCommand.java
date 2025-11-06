package com.klp.hub.hub.application.command.hub;

public record UpdateHubCommand(
    String name,
    Long latitude,
    Long longitude,
    String address
) {

}
