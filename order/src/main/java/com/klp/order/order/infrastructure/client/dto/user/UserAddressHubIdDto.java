package com.klp.order.order.infrastructure.client.dto.user;

import com.klp.order.order.domain.vo.UserAddressHubId;
import java.util.UUID;

public record UserAddressHubIdDto(
    UUID userAddressHubId,
    String address
) {

    public UserAddressHubId toVo() {
        return new UserAddressHubId(
            userAddressHubId,
            address
        );
    }
}
