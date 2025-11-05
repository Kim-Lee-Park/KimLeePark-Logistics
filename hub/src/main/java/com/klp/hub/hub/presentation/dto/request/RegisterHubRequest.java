package com.klp.hub.hub.presentation.dto.request;

import com.klp.hub.hub.application.command.RegisterHubCommand;

public record RegisterHubRequest(
    String name,
    Long latitude,
    Long longitude,
    String address
) {
    public RegisterHubCommand toCommand(RegisterHubRequest dto){
        return new RegisterHubCommand(
            dto.name(),
            dto.latitude(),
            dto.longitude(),
            dto.address()
        );
    }
}