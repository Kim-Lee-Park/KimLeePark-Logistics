package com.klp.order.domain.vo;

import java.util.UUID;

public record UserAddress(
    UUID userAddressHubId,
    String address
) {

}
