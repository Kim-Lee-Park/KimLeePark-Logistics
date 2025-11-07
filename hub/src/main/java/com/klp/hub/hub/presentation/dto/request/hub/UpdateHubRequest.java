package com.klp.hub.hub.presentation.dto.request.hub;

import com.klp.hub.hub.application.command.hub.UpdateHubCommand;

public record UpdateHubRequest(
    String name,
    Long latitude,
    Long longitude,
    String address
) {
    public UpdateHubCommand toCommand(){
        return new UpdateHubCommand(
            name,
            latitude,
            longitude,
            address
        );
    }
}
