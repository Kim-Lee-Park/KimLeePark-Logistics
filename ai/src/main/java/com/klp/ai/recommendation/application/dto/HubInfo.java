package com.klp.ai.recommendation.application.dto;

import java.util.UUID;

public record HubInfo(
    UUID hubId,
    String hubName,
    Double latitude,
    Double longitude,
    String address
) {

}
