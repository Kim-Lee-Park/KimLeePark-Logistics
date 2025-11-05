package com.klp.hub.hub.presentation.dto.request.hub;

import com.klp.hub.hub.application.command.UpdateHubCommand;

public record UpdateHubRequest(
    String name,
    Long latitude,
    Long longitude,
    String address
) {
    public UpdateHubCommand toCommand(UpdateHubRequest dto){
        return new UpdateHubCommand(
            dto.name(),
            dto.latitude(),
            dto.longitude(),
            dto.address()
        );
    }
}
