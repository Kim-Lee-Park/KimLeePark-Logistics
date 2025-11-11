package com.klp.user.presentation.dto.response;

import com.klp.user.domain.entity.User;

public record UserInfoResponse(
    Long userId,
    String affiliationName,
    String username,
    String slackId,
    String phone,
    String role,
    String status
) {

    public static UserInfoResponse of(User user, String affiliationName) {
        return new UserInfoResponse(
            user.getUserId(),
            affiliationName,
            user.getName(),
            user.getSlackId(),
            user.getPhone(),
            user.getRole().name(),
            user.getStatus().name()
        );
    }
}
