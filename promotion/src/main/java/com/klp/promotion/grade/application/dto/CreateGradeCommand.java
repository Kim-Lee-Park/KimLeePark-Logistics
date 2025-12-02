package com.klp.promotion.grade.application.dto;

public record CreateGradeCommand(
    String gradeName,
    Integer benefitDiscountRate,
    Long minAmount,
    Long maxAmount
) {

}
