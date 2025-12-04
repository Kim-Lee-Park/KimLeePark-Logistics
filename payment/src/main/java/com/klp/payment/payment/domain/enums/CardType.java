package com.klp.payment.payment.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CardType {

    SHINHAN("신한카드"),
    KB("KB국민카드"),
    SAMSUNG("삼성카드"),
    HYUNDAI("현대카드"),
    LOTTE("롯데카드"),
    HANA("하나카드"),
    BC("BC카드"),
    NH("NH농협카드"),
    WOORI("우리카드"),
    IBK("IBK기업은행카드"),
    CITI("씨티카드"),
    KAKAO("카카오뱅크카드"),
    TOSS("토스뱅크카드"),
    KBANK("케이뱅크카드");

    private final String description;
}
