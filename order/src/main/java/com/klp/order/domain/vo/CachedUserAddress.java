package com.klp.order.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CachedUserAddress {

    private final UserAddress userAddress;
    private final boolean negative;

    public boolean isNegative() {
        return negative;
    }
}
