package com.klp.user.infrastructure.client.dto.response;

import java.util.UUID;

public record HubDetailResponse(
    UUID hubId,
    String name,
    Double latitude,
    Double longitude,
    String address,
    String status
) {
}
