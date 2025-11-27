package com.klp.hub.hub.presentation.dto.response;

import com.klp.hub.hub.domain.model.Hub;
import java.util.UUID;

public record GetHubByNameResponse(
    UUID hubId,
    String hubName
) {

    public static GetHubByNameResponse from(Hub hub) {
        return new GetHubByNameResponse(
            hub.getHubId(),
            hub.getName()
        );
    }
}
