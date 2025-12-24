package com.klp.user.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {
    PENDING("승인 대기 중"),
    APPROVED("승인"),
    REJECTED("거절");

    private final String description;
}
