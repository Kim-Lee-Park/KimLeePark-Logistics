package com.klp.order.domain.vo;

public record UserProfile(
    Long userId,
    String username,
    String affiliation,
    String slackId,
    String phoneNumber,
    String email,
    String grade
) {

}
