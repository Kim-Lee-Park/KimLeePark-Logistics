package com.klp.hub.hub.application.command.hub;

public record RegisterHubCommand(
    String name,
    Double latitude,
    Double longitude,
    String address
) {
}
