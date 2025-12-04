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
    boolean activate
) {

    public static UserDetailResponse of(String affiliationName, User user) {
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
            user.getStatus() == UserStatus.APPROVED
        );
    }
}
