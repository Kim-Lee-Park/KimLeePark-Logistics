package com.klp.user.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    MASTER("마스터"),
    HUB("허브 관리자"),
    COMPANY("업체 담당자"),
    DRIVER("배송 담당자");

    private final String description;
}
