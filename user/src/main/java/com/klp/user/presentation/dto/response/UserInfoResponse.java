package com.klp.user.presentation.dto.response;

import com.klp.user.domain.entity.User;

public record UserInfoResponse(
    Long userId,
    String affiliationName,
    String username,
    String slackId,
    String phone,
    String email,
    String role,
    String status,
    String gradeName
) {

    public static UserInfoResponse of(User user, String affiliationName, String gradeName) {
        return new UserInfoResponse(
            user.getUserId(),
            affiliationName,
            user.getName(),
            user.getSlackId(),
            user.getPhone(),
            user.getEmail(),
            user.getRole().name(),
            user.getStatus().name(),
            gradeName
        );
    }
}
