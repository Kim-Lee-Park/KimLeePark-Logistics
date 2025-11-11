package com.klp.user.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    MASTER("마스터"),
    HUB("허브 관리자"),
    COMPANY("업체 담당자"),
    HUB_DRIVER("허브 배송 담당자"),
    COMPANY_DRIVER("업체 배송 담당자");

    private final String description;
}
