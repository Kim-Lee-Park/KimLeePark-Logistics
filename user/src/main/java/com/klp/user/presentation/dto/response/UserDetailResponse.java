package com.klp.user.presentation.dto.response;

import com.klp.user.domain.entity.User;
import com.klp.user.domain.enums.UserStatus;

public record UserDetailResponse(
    Long userId,
    String affiliationName,
    String username,
    String slackId,
    String phone,
    String email,
    String role,
    boolean activate
) {

    public static UserDetailResponse of(String affiliationName, User user) {
        return new UserDetailResponse(
            user.getUserId(),
            affiliationName,
            user.getName(),
            user.getSlackId(),
            user.getPhone(),
            user.getEmail(),
            user.getRole().name(),
            user.getStatus() == UserStatus.APPROVED
        );
    }
}
