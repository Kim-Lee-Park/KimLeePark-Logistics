package com.klp.hub.product.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductCategory {
    ELECTRONICS("전자제품"),
    HOME_APPLIANCE("생활가전"),
    KITCHEN_APPLIANCE("주방가전"),
    FURNITURE("가구"),
    FOOD("식품"),
    BEVERAGE("음료"),
    CLOTHING("의류"),
    COMPUTER_PARTS("컴퓨터부품"),
    COMPUTER_PERIPHERAL("컴퓨터주변기기"),
    AUTO_PARTS("자동차부품"),
    ELECTRONIC_PARTS("전자부품"),
    PHARMACEUTICAL("의약품"),
    INDUSTRIAL("산업재"),
    ETC("기타");

    private final String displayName;
}
