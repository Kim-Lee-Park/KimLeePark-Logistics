package com.klp.promotion.grade.application.dto;

public record UpdateGradeCommand(
    String gradeName,
    Integer benefitDiscountRate,
    Long minAmount,
    Long maxAmount
) {

}
