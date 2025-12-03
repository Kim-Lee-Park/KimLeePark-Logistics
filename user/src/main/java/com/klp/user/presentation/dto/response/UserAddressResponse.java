package com.klp.user.presentation.dto.response;

import com.klp.user.domain.entity.UserAddress;
import java.util.UUID;

public record UserAddressResponse(
    UUID userAddressId,
    UUID hubId,
    String address,
    String detail,
    boolean isDefault,
    Double latitude,
    Double longitude
) {

    public static UserAddressResponse from(UserAddress userAddress) {
        return new UserAddressResponse(
            userAddress.getUserAddressId(),
            userAddress.getHubId(),
            userAddress.getAddress(),
            userAddress.getDetail(),
            userAddress.isDefault(),
            userAddress.getLatitude(),
            userAddress.getLongitude()
        );
    }
}
