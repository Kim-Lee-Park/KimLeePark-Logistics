package com.klp.order.order.infrastructure.client.dto.user;

import com.klp.order.order.domain.vo.UserProfile;
import java.util.UUID;

public record UserProfileDto(
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

    public UserProfile toVo() {
        return new UserProfile(
            userId,
            username,
            affiliationName,
            slackId,
            phone,
            email,
            gradeName
        );
    }
}
