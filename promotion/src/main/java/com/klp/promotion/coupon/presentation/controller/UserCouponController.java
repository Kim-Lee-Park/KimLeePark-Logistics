package com.klp.promotion.coupon.presentation.controller;

import com.klp.promotion.coupon.application.facade.UserCouponFacade;
import com.klp.promotion.coupon.application.service.UserCouponService;
import com.klp.promotion.coupon.presentation.dto.IssueUserCouponResponse;
import com.klp.promotion.coupon.presentation.dto.UserCouponDetailResponse;
import com.klp.promotion.global.security.model.UserDetailsImpl;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/user-coupons")
@RequiredArgsConstructor
public class UserCouponController {

    private final UserCouponFacade userCouponFacade;
    private final UserCouponService userCouponService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<IssueUserCouponResponse> issueUserCoupon(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestBody UUID couponId) {
        IssueUserCouponResponse response = userCouponFacade.issueUserCoupon(couponId, userDetails.getUserId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{couponId}/use")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Void> useUserCoupon(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable UUID couponId) {
        userCouponFacade.useUserCoupon(couponId, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{userCouponId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<UserCouponDetailResponse> getUserCoupon(
        @PathVariable UUID userCouponId) {
        UserCouponDetailResponse response = UserCouponDetailResponse.from(userCouponService.findByUserCouponId(userCouponId));
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<UserCouponDetailResponse>> getUserCoupons(
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<UserCouponDetailResponse> response = UserCouponDetailResponse.fromList(userCouponService.findAllByUserId(userDetails.getUserId()));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userCouponId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Void> deleteUserCoupons(
        @AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable UUID userCouponId) {
        userCouponFacade.deleteUserCoupon(userCouponId, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }



}
