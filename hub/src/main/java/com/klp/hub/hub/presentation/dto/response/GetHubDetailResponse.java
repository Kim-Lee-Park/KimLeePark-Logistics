package com.klp.hub.hub.presentation.dto.response;

import java.util.UUID;

public record GetHubDetailResponse(
    UUID hubId,
    String name,
    Long latitude,
    Long longitude,
    String address
) {

}
