package com.klp.hub.hub.presentation.dto.response.hub;

import com.klp.hub.hub.domain.model.Hub;
import com.klp.hub.hub.domain.model.HubStatus;
import java.util.UUID;

public record GetHubDetailResponse(
    UUID hubId,
    String name,
    Long latitude,
    Long longitude,
    String address,
    HubStatus status
) {
    public static GetHubDetailResponse from(Hub hub) {
        return new GetHubDetailResponse(
            hub.getHubId(),
            hub.getName(),
            hub.getLatitude(),
            hub.getLongitude(),
            hub.getAddress(),
            hub.getStatus()
        );
    }
}
