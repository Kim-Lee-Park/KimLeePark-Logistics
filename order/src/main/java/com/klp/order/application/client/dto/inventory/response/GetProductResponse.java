package com.klp.order.application.client.dto.inventory.response;

import java.util.UUID;

public record GetProductResponse(
    UUID productId,
    UUID hubId,
    String companyName,
    String productName
) {

}
