package com.klp.promotion.coupon.presentation.controller;

import com.klp.promotion.coupon.application.service.CouponService;
import com.klp.promotion.coupon.presentation.dto.CouponDetailResponse;

import com.klp.promotion.coupon.presentation.dto.CouponResponse;
import com.klp.promotion.coupon.presentation.dto.CreateCouponRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/promotions/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    public ResponseEntity<CouponResponse> createCoupon(@RequestBody @Valid CreateCouponRequest request){
        return ResponseEntity.ok(couponService.createCoupon(request));
    }


    @GetMapping("/{couponId}")
    public ResponseEntity<CouponDetailResponse> getCoupon(@PathVariable UUID couponId){
        CouponDetailResponse response = CouponDetailResponse.from(couponService.findByCouponId(couponId));
        return ResponseEntity.ok(response);
    }



}
