package com.klp.promotion.coupon.domain.entity;


import static com.klp.promotion.coupon.common.exception.CouponErrorCode.CANNOT_USE_UNRESERVED_COUPON;
import static com.klp.promotion.coupon.common.exception.CouponErrorCode.COUPON_ALREADY_USED;
import static com.klp.promotion.coupon.common.exception.CouponErrorCode.COUPON_NOT_FOUND;
import static com.klp.promotion.coupon.common.exception.CouponErrorCode.COUPON_NOT_USABLE;

import com.klp.promotion.common.model.BaseEntity;
import com.klp.promotion.coupon.domain.enums.UserCouponStatus;
import com.klp.promotion.global.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@Table(name = "p_user_coupons", schema = "promotion_schema")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCoupon extends BaseEntity {

    @Version
    private Integer version;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_coupon_id", nullable = false)
    @Comment("유저 쿠폰 ID")
    private UUID userCouponId;

    @Comment("쿠폰 ID")
    @Column(name = "coupon_id", nullable = false)
    private UUID couponId;

    @Comment("유저 ID")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Comment("쿠폰 상태")
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserCouponStatus status;


    @Comment("쿠폰 사용 시간")
    @Column(name = "used_at")
    private LocalDateTime usedAt;


    private UserCoupon(UUID couponId, Long userId, UserCouponStatus status) {
        this.couponId = couponId;
        this.userId = userId;
        this.status = status;
    }


    public static UserCoupon create(UUID couponId, Long userId) {
        return new UserCoupon(couponId, userId, UserCouponStatus.READY);
    }


    public void confirmUse() {
        if(this.status != UserCouponStatus.RESERVE){
            throw new BusinessException(CANNOT_USE_UNRESERVED_COUPON);
        }
        this.status = UserCouponStatus.USED;
        this.usedAt = LocalDateTime.now();
    }


    public void deleteValidate() {
        if (status == UserCouponStatus.USED) {
            throw new BusinessException(COUPON_ALREADY_USED);
        }
    }

    public void validateStatus() {
        if (status == UserCouponStatus.USED || isDeleted() || status == UserCouponStatus.EXPIRED) {
            throw new BusinessException(COUPON_NOT_USABLE);
        }
    }

    public static void validateUsable(UserCoupon userCoupon) {
        if (userCoupon == null) {
            throw new BusinessException(COUPON_NOT_FOUND);
        }
        userCoupon.validateStatus();
    }

    public void releaseReserve(){
        this.status = UserCouponStatus.READY;
    }
}
