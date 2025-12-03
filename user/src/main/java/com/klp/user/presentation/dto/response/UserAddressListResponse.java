package com.klp.user.presentation.dto.response;

import java.util.List;

public record UserAddressListResponse(
    List<UserAddressResponse> addresses
) {

    public static UserAddressListResponse of(List<UserAddressResponse> addresses) {
        return new UserAddressListResponse(addresses);
    }
}
