package com.klp.user.presentation.dto.response;

import com.klp.user.domain.entity.User;
import java.util.UUID;

public record DriverDetailResponse(
    Long userId,
    UUID hubId,
    String username,
    String slackId,
    String phone,
    String email
) {
    public static DriverDetailResponse from(User user) {
        return new DriverDetailResponse(
            user.getUserId(),
            user.getAffiliationId(),
            user.getName(),
            user.getSlackId(),
            user.getPhone(),
            user.getEmail()
        );
    }
}
