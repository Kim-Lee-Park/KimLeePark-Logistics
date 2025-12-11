package com.klp.ai.recommendation.infrastructure.client.dto.response;

import java.util.UUID;

public record HubResponse(
    UUID hubId,
    String name,
    Double latitude,
    Double longitude,
    String address
) {

}
