package com.klp.user.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AffiliationType {
    LOGISTICS("물류 회사"),
    COMPANY("업체"),
    HUB("허브");

    private final String description;
}
