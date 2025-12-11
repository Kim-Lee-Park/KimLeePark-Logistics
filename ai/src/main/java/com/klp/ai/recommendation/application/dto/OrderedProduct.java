package com.klp.ai.recommendation.application.dto;

import java.util.UUID;

public record OrderedProduct(
    UUID productId,
    String productName,
    String companyName
) {

}
