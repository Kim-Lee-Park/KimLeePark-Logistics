package com.klp.user.infrastructure.client.dto.request;

public record NearestHubRequest(
    Double latitude,
    Double longitude
) {
}
