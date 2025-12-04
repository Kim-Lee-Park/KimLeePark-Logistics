package com.klp.promotion.coupon.presentation.dto;

import com.klp.promotion.coupon.application.command.CouponCommand;
import com.klp.promotion.coupon.domain.enums.CouponType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CreateCouponRequest(
    @NotBlank(message = "쿠폰명은 필수입니다.")
    String name,

    @NotNull(message = "할인 타입은 필수입니다.")
    CouponType discount_type,

    @NotNull
    @Min(value = 1, message = "할인 값은 1 이상이어야 합니다.")
    Long discount_value,

    @Min(0)
    int min_amount,

    @NotNull
    @Min(1)
    Long max_discount_amount,

    @NotNull
    @Min(value = 1,  message = "총 수량은 1 이상이어야 합니다")
    Long total_quantity,

    @NotNull(message = "유효기간 만료일은 필수 입니다")
    LocalDateTime expired_at
) {

    public CouponCommand toCommand() {

        return new CouponCommand(name, discount_type, discount_value, min_amount, max_discount_amount, total_quantity, expired_at);

    }
}
