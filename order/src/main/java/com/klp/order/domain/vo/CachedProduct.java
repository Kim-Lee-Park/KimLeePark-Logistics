package com.klp.order.domain.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 상품 캐시 래퍼 클래스
@Getter
@RequiredArgsConstructor
public class CachedProduct {

    private final Product product;
    private final boolean negative;

    public boolean isNegative() {
        return negative;
    }
}
