package com.klp.promotion.coupon.presentation.controller;


import com.klp.promotion.coupon.application.facade.UserCouponFacade;
import com.klp.promotion.coupon.presentation.dto.CouponApplyRequest;
import com.klp.promotion.coupon.presentation.dto.CouponApplyResponse;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/internal/promotions/user/coupon")
@RequiredArgsConstructor
@Hidden
public class UserCouponInternalController {

    private final UserCouponFacade facade;

    @PostMapping("/apply")
    public ResponseEntity<CouponApplyResponse> applyCoupon(@RequestBody CouponApplyRequest couponApplyRequest) {
        return ResponseEntity.ok().body(facade.applyUserCoupon(
            couponApplyRequest.userCouponId(), couponApplyRequest.gradeName(), couponApplyRequest.originalPrice()));
    }

}
