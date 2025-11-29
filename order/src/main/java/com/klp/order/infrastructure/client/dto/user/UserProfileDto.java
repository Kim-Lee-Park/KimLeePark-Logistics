package com.klp.order.infrastructure.client.dto.user;

import com.klp.order.domain.vo.UserProfile;

public record UserProfileDto(
    Long userId,
    String name,
    String affiliation,
    String slackId,
    String phoneNumber,
    String grade
) {

    public UserProfile toVo() {
        return UserProfile.builder()
            .userId(userId)
            .name(name)
            .affiliation(affiliation)
            .slackId(slackId)
            .phoneNumber(phoneNumber)
            .grade(grade)
            .build();
    }
}
