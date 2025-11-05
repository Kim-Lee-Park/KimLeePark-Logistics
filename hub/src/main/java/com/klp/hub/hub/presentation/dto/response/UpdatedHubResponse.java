package com.klp.hub.hub.presentation.dto.response;

import java.util.UUID;

public record UpdatedHubResponse(
    UUID hubId,
    String name,
    Long latitude,
    Long longitude,
    String address
) {

}
