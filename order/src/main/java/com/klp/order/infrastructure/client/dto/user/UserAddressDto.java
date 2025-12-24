package com.klp.order.infrastructure.client.dto.user;

import com.klp.order.domain.vo.UserAddress;
import java.util.UUID;

public record UserAddressDto(
    UUID userAddressHubId,
    String address
) {

    public UserAddress toVo() {
        return new UserAddress(userAddressHubId, address);
    }
}
