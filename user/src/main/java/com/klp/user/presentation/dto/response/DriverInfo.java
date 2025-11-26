package com.klp.user.presentation.dto.response;

import com.klp.user.domain.entity.User;

public record DriverInfo(
    Long userId,
    String username,
    String slackId,
    String phone
) {
    public static DriverInfo from(User user) {
        return new DriverInfo(
            user.getUserId(),
            user.getName(),
            user.getSlackId(),
            user.getPhone()
        );
    }
}
