package com.klp.ai.recommendation.infrastructure.client.feign.dto.response;

import java.util.UUID;

public record ProductResponse(
    UUID productId,
    UUID hubId,
    String companyName,
    String productName
) {

}
