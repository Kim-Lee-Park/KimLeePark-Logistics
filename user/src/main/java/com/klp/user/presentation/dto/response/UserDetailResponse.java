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
    String gradeName,
    boolean activate
) {

    public static UserDetailResponse of(User user, String affiliationName, String gradeName) {
        return new UserDetailResponse(
            user.getUserId(),
            affiliationName,
            user.getName(),
            user.getSlackId(),
            user.getPhone(),
            user.getEmail(),
            user.getRole().name(),
            gradeName,
            user.getStatus() == UserStatus.APPROVED
        );
    }
}
