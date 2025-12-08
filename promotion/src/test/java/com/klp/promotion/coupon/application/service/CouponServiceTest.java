package com.klp.promotion.coupon.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.klp.common.exception.BusinessException;
import com.klp.promotion.coupon.common.exception.CouponErrorCode;
import com.klp.promotion.coupon.domain.entity.Coupon;
import com.klp.promotion.coupon.domain.enums.CouponType;
import com.klp.promotion.coupon.infrastructure.repository.CouponJpaRepositroy;
import com.klp.promotion.coupon.presentation.dto.CouponResponse;
import com.klp.promotion.coupon.presentation.dto.CreateCouponRequest;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("CouponService 통합 테스트")
class CouponServiceTest {

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponJpaRepositroy couponJpaRepositroy;

    @AfterEach
    @Transactional
    void tearDown() {
        couponJpaRepositroy.deleteAll();
    }

    @Test
    @DisplayName("쿠폰 생성 성공 - FIXED 타입")
    @Transactional
    void createCoupon_Success_Fixed() {
        // given
        CreateCouponRequest request = new CreateCouponRequest(
            "정액 할인 쿠폰",
            CouponType.FIXED,
            5000L,
            0,
            5000L,
            100L,
            LocalDateTime.now().plusDays(30)
        );

        // when
        CouponResponse response = couponService.createCoupon(request);

        // then
        assertThat(response.couponId()).isNotNull();
        
        Coupon savedCoupon = couponJpaRepositroy.findByCouponId(response.couponId());
        assertThat(savedCoupon).isNotNull();
        assertThat(savedCoupon.getName()).isEqualTo("정액 할인 쿠폰");
        assertThat(savedCoupon.getDiscount_type()).isEqualTo(CouponType.FIXED);
        assertThat(savedCoupon.getDiscount_value()).isEqualTo(5000L);
        assertThat(savedCoupon.getMin_amount()).isEqualTo(0);
        assertThat(savedCoupon.getMax_discount_amount()).isEqualTo(5000L);
        assertThat(savedCoupon.getTotal_quantity()).isEqualTo(100L);
        assertThat(savedCoupon.getRemain_quantity()).isEqualTo(100L);
    }

    @Test
    @DisplayName("쿠폰 생성 성공 - RATE 타입")
    @Transactional
    void createCoupon_Success_Rate() {
        // given
        CreateCouponRequest request = new CreateCouponRequest(
            "정률 할인 쿠폰",
            CouponType.RATE,
            10L,
            10000,
            5000L,
            100L,
            LocalDateTime.now().plusDays(30)
        );

        // when
        CouponResponse response = couponService.createCoupon(request);

        // then
        assertThat(response.couponId()).isNotNull();
        
        Coupon savedCoupon = couponJpaRepositroy.findByCouponId(response.couponId());
        assertThat(savedCoupon).isNotNull();
        assertThat(savedCoupon.getName()).isEqualTo("정률 할인 쿠폰");
        assertThat(savedCoupon.getDiscount_type()).isEqualTo(CouponType.RATE);
        assertThat(savedCoupon.getDiscount_value()).isEqualTo(10L);
        assertThat(savedCoupon.getMin_amount()).isEqualTo(10000);
        assertThat(savedCoupon.getMax_discount_amount()).isEqualTo(5000L);
        assertThat(savedCoupon.getTotal_quantity()).isEqualTo(100L);
        assertThat(savedCoupon.getRemain_quantity()).isEqualTo(100L);
    }

}

