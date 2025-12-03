package com.klp.promotion.grade.presentation.dto.response;

import com.klp.promotion.grade.domain.entity.Grade;
import java.util.UUID;

public record GradeResponse(
    UUID gradeId,
    String gradeName,
    Integer benefitDiscountRate,
    Long minAmount,
    Long maxAmount
) {

    public static GradeResponse from(Grade grade) {
        return new GradeResponse(
            grade.getGradeId(),
            grade.getGradeName(),
            grade.getBenefitDiscountRate(),
            grade.getMinAmount(),
            grade.getMaxAmount()
        );
    }
}
