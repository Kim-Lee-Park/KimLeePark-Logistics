package com.klp.promotion.coupon.presentation.controller;

import com.klp.promotion.coupon.application.facade.UserCouponFacade;
import com.klp.promotion.coupon.presentation.dto.IssueUserCouponResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/user-coupons")
@RequiredArgsConstructor
public class UserCouponController {

    private final UserCouponFacade userCouponFacade;

    @PostMapping
    public ResponseEntity<IssueUserCouponResponse> issueUserCoupon(
        @RequestHeader("X-USER-ID") Long userId,
        @RequestBody UUID couponId) {
        IssueUserCouponResponse response = userCouponFacade.issueUserCoupon(couponId, userId);
        return ResponseEntity.ok(response);
    }


}
