package com.klp.authservice.auth.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AffiliationType {
    COMPANY("업체"),
    HUB("허브");

    private final String description;
}
