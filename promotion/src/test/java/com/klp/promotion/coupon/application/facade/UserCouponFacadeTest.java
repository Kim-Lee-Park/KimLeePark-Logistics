package com.klp.promotion.coupon.application.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.klp.common.exception.BusinessException;
import com.klp.promotion.coupon.MockTest;
import com.klp.promotion.coupon.application.service.CouponService;
import com.klp.promotion.coupon.application.service.UserCouponService;
import com.klp.promotion.coupon.common.exception.CouponErrorCode;
import com.klp.promotion.coupon.domain.entity.Coupon;
import com.klp.promotion.coupon.domain.entity.UserCoupon;
import com.klp.promotion.coupon.domain.enums.CouponType;
import com.klp.promotion.coupon.presentation.dto.IssueUserCouponResponse;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("CouponFacade 테스트")
class UserCouponFacadeTest extends MockTest {

    @Mock
    private UserCouponService userCouponService;

    @Mock
    private CouponService couponService;

    @InjectMocks
    private UserCouponFacade userCouponFacade;

    @Test
    @DisplayName("쿠폰 발급 성공")
    void createCoupon_Success() {
        // given
        UUID couponId = UUID.randomUUID();
        Long userId = 1L;

        Coupon coupon = Coupon.create("테스트 쿠폰", CouponType.RATE, 10L, 10000, 5000L, 100L, 50L,
            LocalDateTime.now().plusDays(30));
        UserCoupon userCoupon = UserCoupon.create(couponId, userId);

        when(userCouponService.findByUserIdAndCouponId(userId, couponId)).thenReturn(null);
        when(couponService.decreaseStock(couponId)).thenReturn(true);
        when(userCouponService.createUserCoupon(userId, couponId)).thenReturn(userCoupon);
        when(couponService.updateStock(couponId)).thenReturn(coupon);

        // when
        IssueUserCouponResponse response = userCouponFacade.issueUserCoupon(couponId, userId);

        // then
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("중복 쿠폰 발급 실패")
    void createCoupon_Fail_AlreadyIssued() {
        // given
        UUID couponId = UUID.randomUUID();
        Long userId = 1L;
        UserCoupon existingUserCoupon = UserCoupon.create(couponId, userId);

        when(userCouponService.findByUserIdAndCouponId(userId, couponId))
            .thenReturn(existingUserCoupon);

        // when & then
        assertThatThrownBy(() -> userCouponFacade.issueUserCoupon(couponId, userId))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", CouponErrorCode.COUPON_ALREADY_ISSUED);
    }

    @Test
    @DisplayName("재고 부족으로 쿠폰 발급 실패")
    void createCoupon_Fail_OutOfStock() {
        // given
        UUID couponId = UUID.randomUUID();
        Long userId = 1L;

        when(userCouponService.findByUserIdAndCouponId(userId, couponId)).thenReturn(null);
        when(couponService.decreaseStock(couponId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> userCouponFacade.issueUserCoupon(couponId, userId))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", CouponErrorCode.COUPON_OUT_OF_STOCK);
    }
}

