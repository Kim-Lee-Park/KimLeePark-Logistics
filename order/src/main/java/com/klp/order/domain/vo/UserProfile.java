package com.klp.order.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfile {

    private Long userId;
    private String username;
    private String affiliation;
    private String slackId;
    private String phoneNumber;
    private String grade;
}
