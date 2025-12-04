package com.klp.payment.payment.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethodType {

    // 지금은 카드 타입만 지원
    CARD("카드");

    private final String description;
}
