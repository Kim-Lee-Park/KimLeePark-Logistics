package com.klp.hub.hub.application.command;

public record RegisterHubCommand(
    String name,
    Long latitude,
    Long longitude,
    String address
) {
}
