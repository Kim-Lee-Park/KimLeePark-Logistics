package com.klp.user.application.command;

import java.util.UUID;

public record UserAddressCreateCommand(
    Long userId,
    UUID hubId,
    String address,
    String detail,
    boolean isDefault,
    Double latitude,
    Double longitude
) {

}
