package com.klp.promotion.grade.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GradeType {
    NONE("등급 없음", 0, 0L, 1_000_000L),
    BRONZE("브론즈", 4, 1_010_000L, 5_000_000L),
    SILVER("실버", 7, 5_010_000L, 10_000_000L),
    GOLD("골드", 10, 10_010_000L, Long.MAX_VALUE);

    private final String displayName;
    private final int discountRate;
    private final long minAmount;
    private final long maxAmount;

    public static GradeType calculateGrade(Long totalAmount) {
        if (totalAmount == null || totalAmount < 0) {
            return NONE;
        }

        for (GradeType gradeType : values()) {
            if (totalAmount >= gradeType.minAmount && totalAmount <= gradeType.maxAmount) {
                return gradeType;
            }
        }

        return GOLD;
    }
}
