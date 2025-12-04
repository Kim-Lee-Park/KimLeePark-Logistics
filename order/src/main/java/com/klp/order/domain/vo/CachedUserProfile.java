package com.klp.order.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

/*
유저 프로필 캐시 래퍼 클래스
 */
@Getter
@AllArgsConstructor
public class CachedUserProfile {

    private final UserProfile profile;
    private final boolean negative;

    public boolean isNegative() {
        return negative;
    }
}