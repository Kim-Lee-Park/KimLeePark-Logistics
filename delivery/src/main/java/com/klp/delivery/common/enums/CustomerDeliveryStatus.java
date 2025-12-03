package com.klp.delivery.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CustomerDeliveryStatus {
    CREATED("배송생성"),        // 배송 생성됨
    SHIPPING("배송중"),        // 고객에게 표시되는 "배송중"
    ARRIVED("배송완료");       // 고객에게 표시되는 "배송도착"

    private final String description;
}

