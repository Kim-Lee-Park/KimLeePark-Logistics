package com.klp.promotion.coupon.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.klp.common.exception.BusinessException;
import com.klp.promotion.coupon.common.exception.CouponErrorCode;
import com.klp.promotion.coupon.domain.enums.CouponType;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Coupon 도메인 테스트")
class CouponTest {

    @Test
    @DisplayName("쿠폰 생성 성공 - FIXED 타입")
    void create_Success_Fixed() {
        // given & when
        Coupon coupon = Coupon.create(
            "정액 할인 쿠폰",
            CouponType.FIXED,
            5000L,
            0,
            5000L,
            100L,
            100L,
            LocalDateTime.now().plusDays(30)
        );

        // then
        assertThat(coupon.getName()).isEqualTo("정액 할인 쿠폰");
        assertThat(coupon.getDiscount_type()).isEqualTo(CouponType.FIXED);
        assertThat(coupon.getDiscount_value()).isEqualTo(5000L);
        assertThat(coupon.getMin_amount()).isEqualTo(0);
        assertThat(coupon.getMax_discount_amount()).isEqualTo(5000L);
        assertThat(coupon.getTotal_quantity()).isEqualTo(100L);
        assertThat(coupon.getRemain_quantity()).isEqualTo(100L);
    }

    @Test
    @DisplayName("쿠폰 생성 성공 - RATE 타입")
    void create_Success_Rate() {
        // given & when
        Coupon coupon = Coupon.create(
            "정률 할인 쿠폰",
            CouponType.RATE,
            10L,
            10000,
            5000L,
            100L,
            100L,
            LocalDateTime.now().plusDays(30)
        );

        // then
        assertThat(coupon.getName()).isEqualTo("정률 할인 쿠폰");
        assertThat(coupon.getDiscount_type()).isEqualTo(CouponType.RATE);
        assertThat(coupon.getDiscount_value()).isEqualTo(10L);
        assertThat(coupon.getMin_amount()).isEqualTo(10000);
        assertThat(coupon.getMax_discount_amount()).isEqualTo(5000L);
        assertThat(coupon.getTotal_quantity()).isEqualTo(100L);
        assertThat(coupon.getRemain_quantity()).isEqualTo(100L);
    }

    @Test
    @DisplayName("FIXED 타입 쿠폰 생성 실패 - min_amount가 0이 아님")
    void create_Fail_Fixed_InvalidMinAmount() {
        // given & when & then
        assertThatThrownBy(() -> Coupon.create(
            "정액 할인 쿠폰",
            CouponType.FIXED,
            5000L,
            1000, // min_amount가 0이 아님
            5000L,
            100L,
            100L,
            LocalDateTime.now().plusDays(30)
        ))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", CouponErrorCode.INVALID_COUPON_DISCOUNT_TYPE);
    }

    @Test
    @DisplayName("FIXED 타입 쿠폰 생성 실패 - max_discount_amount가 discount_value와 다름")
    void create_Fail_Fixed_InvalidMaxDiscountAmount() {
        // given & when & then
        assertThatThrownBy(() -> Coupon.create(
            "정액 할인 쿠폰",
            CouponType.FIXED,
            5000L,
            0,
            3000L, // max_discount_amount가 discount_value와 다름
            100L,
            100L,
            LocalDateTime.now().plusDays(30)
        ))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", CouponErrorCode.INVALID_COUPON_DISCOUNT_TYPE);
    }

    @Test
    @DisplayName("RATE 타입 쿠폰 생성 실패 - min_amount가 0 이하")
    void create_Fail_Rate_InvalidMinAmount() {
        // given & when & then
        assertThatThrownBy(() -> Coupon.create(
            "정률 할인 쿠폰",
            CouponType.RATE,
            10L,
            0, // min_amount가 0 이하
            5000L,
            100L,
            100L,
            LocalDateTime.now().plusDays(30)
        ))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", CouponErrorCode.INVALID_COUPON_DISCOUNT_TYPE);
    }

    @Test
    @DisplayName("쿠폰 생성 실패 - 만료일이 과거")
    void create_Fail_ExpiredAtInPast() {
        // given & when & then
        assertThatThrownBy(() -> Coupon.create(
            "테스트 쿠폰",
            CouponType.RATE,
            10L,
            10000,
            5000L,
            100L,
            100L,
            LocalDateTime.now().minusDays(1) // 과거 날짜
        ))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", CouponErrorCode.INVALID_COUPON_EXPIRED_AT);
    }

    @Test
    @DisplayName("재고 사용 성공")
    void useStock_Success() {
        // given
        Coupon coupon = Coupon.create(
            "테스트 쿠폰",
            CouponType.RATE,
            10L,
            10000,
            5000L,
            100L,
            50L, // remain_quantity = 50
            LocalDateTime.now().plusDays(30)
        );

        // when
        coupon.useStock();

        // then
        assertThat(coupon.getRemain_quantity()).isEqualTo(49L);
    }

    @Test
    @DisplayName("재고 사용 실패 - 재고 부족")
    void useStock_Fail_OutOfStock() {
        // given
        Coupon coupon = Coupon.create(
            "테스트 쿠폰",
            CouponType.RATE,
            10L,
            10000,
            5000L,
            100L,
            0L, // remain_quantity = 0
            LocalDateTime.now().plusDays(30)
        );

        // when & then
        assertThatThrownBy(() -> coupon.useStock())
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", CouponErrorCode.COUPON_OUT_OF_STOCK);
    }
}

