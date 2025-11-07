package com.klp.hub.hub.presentation.dto.response.hub;

import com.klp.hub.hub.domain.model.Hub;
import com.klp.hub.hub.domain.model.HubStatus;
import java.util.UUID;

public record UpdatedHubResponse(
    UUID hubId,
    String name,
    Long latitude,
    Long longitude,
    String address,
    HubStatus status
) {

    public static UpdatedHubResponse from(Hub hub) {
        return new UpdatedHubResponse(
            hub.getHubId(),
            hub.getName(),
            hub.getLatitude(),
            hub.getLongitude(),
            hub.getAddress(),
            hub.getStatus()
        );
    }
}
