package com.klp.promotion.coupon.presentation.controller;

import com.klp.promotion.coupon.application.service.CouponService;
import com.klp.promotion.coupon.presentation.controller.docs.CouponControllerDoc;
import com.klp.promotion.coupon.presentation.dto.CouponDetailResponse;
import com.klp.promotion.coupon.presentation.dto.CouponResponse;
import com.klp.promotion.coupon.presentation.dto.CreateCouponRequest;
import com.klp.promotion.coupon.presentation.dto.UpdateCouponRequest;
import com.klp.promotion.global.security.model.UserDetailsImpl;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/promotions/coupons")
@RequiredArgsConstructor
public class CouponController implements CouponControllerDoc {

    private final CouponService couponService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CouponResponse> createCoupon(@RequestBody @Valid CreateCouponRequest request){
        return ResponseEntity.ok(couponService.createCoupon(request));
    }


    @GetMapping("/{couponId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CouponDetailResponse> getCoupon(@PathVariable UUID couponId){
        CouponDetailResponse response = CouponDetailResponse.from(couponService.findByCouponId(couponId));
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{couponId}")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<Void> updateCoupon(@PathVariable UUID couponId, @RequestBody
        UpdateCouponRequest request){
        couponService.updateCoupon(couponId, request.name(), request.expiredAt());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{couponId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Void> deleteCoupon(@PathVariable UUID couponId, @AuthenticationPrincipal UserDetailsImpl userDetails){
        couponService.deleteCoupon(couponId, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<Page<CouponDetailResponse>> getCoupons(Pageable pageable){
        Page<CouponDetailResponse> response = couponService.findCoupons(pageable);
        return ResponseEntity.ok(response);
    }

}
