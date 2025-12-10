package com.klp.common.util;

import java.util.concurrent.ThreadLocalRandom;

public final class JitterUtil {

    private JitterUtil() {
    }

    // 랜덤 지터 +-20% (0.8 ~ 1.2 배)
    private static final double DEFAULT_MIN = 0.8;
    private static final double DEFAULT_MAX = 1.2;

    public static long jitterSeconds(long baseSeconds) {
        double factor = ThreadLocalRandom.current().nextDouble(DEFAULT_MIN, DEFAULT_MAX);
        long jittered = Math.round(baseSeconds * factor);
        return Math.max(1, jittered); // 최소 1초 보장
    }
}