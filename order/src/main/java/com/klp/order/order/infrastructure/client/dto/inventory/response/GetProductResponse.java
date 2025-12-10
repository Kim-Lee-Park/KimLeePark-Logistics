package com.klp.order.order.infrastructure.client.dto.inventory.response;

import com.klp.order.order.domain.vo.Product;
import java.util.UUID;

public record GetProductResponse(
    UUID productId,
    UUID hubId,
    String companyName,
    String productName
) {

    public Product toVo() {
        return new Product(productId, hubId, companyName, productName);
    }
}
