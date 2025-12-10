package com.klp.order.order.domain.vo;

import java.util.UUID;

public record Product(
    UUID productId,
    UUID hubId,
    String companyName,
    String productName
) {

}
