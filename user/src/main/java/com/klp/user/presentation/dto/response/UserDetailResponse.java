package com.klp.user.presentation.dto.response;

import com.klp.user.domain.entity.User;
import com.klp.user.domain.enums.UserStatus;
import java.util.UUID;

public record UserDetailResponse(
    Long userId,
    UUID affiliationId,
    String affiliationType,
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
            user.getAffiliationId(),
            user.getAffiliationType().name(),
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
