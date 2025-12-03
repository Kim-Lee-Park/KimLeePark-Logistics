package com.klp.promotion.coupon.presentation.controller;

import com.klp.promotion.coupon.application.facade.CouponFacade;
import com.klp.promotion.coupon.presentation.dto.CreateCouponResponse;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/promotions/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponFacade couponFacade;

    @PostMapping("/{couponId}")
    public ResponseEntity<CreateCouponResponse> createCoupon(@RequestHeader("X-USER-ID") Long userId,
        @PathVariable UUID couponId){
        CreateCouponResponse response = couponFacade.createCoupon(couponId, userId);
        return ResponseEntity.ok(response);
    }



}
