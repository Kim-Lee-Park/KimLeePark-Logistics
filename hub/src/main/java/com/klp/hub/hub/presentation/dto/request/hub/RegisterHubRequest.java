package com.klp.hub.hub.presentation.dto.request.hub;

import com.klp.hub.hub.application.command.hub.RegisterHubCommand;

public record RegisterHubRequest(
    String name,
    Long latitude,
    Long longitude,
    String address
) {
    public RegisterHubCommand toCommand(){
        return new RegisterHubCommand(
            name,
            latitude,
            longitude,
            address
        );
    }
}