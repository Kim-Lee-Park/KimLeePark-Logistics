package com.klp.promotion.coupon.presentation.dto;

import com.klp.promotion.coupon.application.command.CouponCommand;
import com.klp.promotion.coupon.domain.enums.CouponType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record UpdateCouponRequest(
    String name,
    LocalDateTime expiredAt
) {
}
