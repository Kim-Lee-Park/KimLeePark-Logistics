package com.klp.hub.hub.application.command.hub;

public record RegisterHubCommand(
    String name,
    Long latitude,
    Long longitude,
    String address
) {
}
