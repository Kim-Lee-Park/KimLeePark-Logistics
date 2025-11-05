package com.klp.hub.hub.presentation.dto.response.hub;

import java.util.UUID;

public record UpdatedHubResponse(
    UUID hubId,
    String name,
    Long latitude,
    Long longitude,
    String address
) {

}
