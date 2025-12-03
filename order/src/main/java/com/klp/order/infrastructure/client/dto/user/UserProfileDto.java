package com.klp.order.infrastructure.client.dto.user;

import com.klp.order.domain.vo.UserProfile;

public record UserProfileDto(
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

    public UserProfile toVo() {
        return UserProfile.builder()
            .userId(userId)
            .username(username)
            .affiliation(affiliationName)
            .slackId(slackId)
            .phoneNumber(phone)
            .grade(gradeName)
            .build();
    }
}
