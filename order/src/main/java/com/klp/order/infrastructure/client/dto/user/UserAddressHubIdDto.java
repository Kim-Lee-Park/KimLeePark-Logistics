package com.klp.order.infrastructure.client.dto.user;

import com.klp.order.domain.vo.UserAddressHubId;
import java.util.UUID;

public record UserAddressHubIdDto(
    UUID userAddressHubId
) {

    public UserAddressHubId toVo() {
        return new UserAddressHubId(
            userAddressHubId
        );
    }
}
