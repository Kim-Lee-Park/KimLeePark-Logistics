package com.klp.promotion.grade.presentation.dto.request;

import com.klp.promotion.grade.application.dto.CreateGradeCommand;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateGradeRequest(
    @NotBlank(message = "등급 이름은 필수입니다")
    String gradeName,

    @NotNull(message = "할인율은 필수입니다")
    @Min(value = 0, message = "할인율은 0 이상이어야 합니다")
    @Max(value = 100, message = "할인율은 100 이하여야 합니다")
    Integer benefitDiscountRate,

    @NotNull(message = "최소 금액은 필수입니다")
    @Min(value = 0, message = "최소 금액은 0 이상이어야 합니다")
    Long minAmount,

    @NotNull(message = "최대 금액은 필수입니다")
    @Min(value = 0, message = "최대 금액은 0 이상이어야 합니다")
    Long maxAmount
) {

    public CreateGradeCommand toCommand() {
        return new CreateGradeCommand(gradeName, benefitDiscountRate, minAmount, maxAmount);
    }
}
