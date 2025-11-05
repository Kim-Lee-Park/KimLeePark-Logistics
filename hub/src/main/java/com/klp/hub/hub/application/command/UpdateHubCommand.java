package com.klp.hub.hub.application.command;

public record UpdateHubCommand(
    String name,
    Long latitude,
    Long longitude,
    String address
) {

}
