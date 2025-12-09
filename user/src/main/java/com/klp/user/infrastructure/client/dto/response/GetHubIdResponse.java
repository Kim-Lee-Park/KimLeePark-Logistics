package com.klp.user.infrastructure.client.dto.response;

import java.util.UUID;

public record GetHubIdResponse(
    UUID hubId,
    String hubName
) {

}
