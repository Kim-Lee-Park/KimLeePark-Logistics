package com.klp.promotion.coupon.domain.entity;

import static com.klp.promotion.coupon.common.exception.CouponErrorCode.COUPON_OUT_OF_STOCK;
import static com.klp.promotion.coupon.common.exception.CouponErrorCode.INVALID_COUPON_DISCOUNT_TYPE;
import static com.klp.promotion.coupon.common.exception.CouponErrorCode.INVALID_COUPON_EXPIRED_AT;

import com.klp.common.exception.BusinessException;
import com.klp.promotion.common.model.BaseEntity;
import com.klp.promotion.coupon.domain.enums.CouponType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@Table(name = "p_coupons", schema = "promotion_schema")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "coupon_id", nullable = false)
    @Comment("쿠폰 ID")
    private UUID couponId;

    @Comment("쿠폰명")
    @Column(name = "name", nullable = false)
    private String name;

    @Comment("할인 방법")
    @Column(name = "discount_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private CouponType discount_type;


    @Comment("할인 값")
    @Column(name = "discount_value", nullable = false)
    private Long discount_value;


    @Comment("최소 사용 금액")
    @Column(name = "min_amount", nullable = false)
    private int min_amount;


    @Comment("최대 할인 금액")
    @Column(name = "max_discount_amount", nullable = false)
    private Long max_discount_amount;

    @Comment("총 수량")
    @Column(name = "total_quantity", nullable = false)
    private Long total_quantity;


    @Comment("남은 수량")
    @Column(name = "remain_quantity", nullable = false)
    private Long remain_quantity;

    @Comment("만료 기간")
    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expired_at;


    private Coupon(String name, CouponType discount_type, Long discount_value, int min_amount,
        Long max_discount_amount, Long total_quantity, Long remain_quantity,
        LocalDateTime expired_at) {
        this.name = name;
        this.discount_type = discount_type;
        this.discount_value = discount_value;
        this.min_amount = min_amount;
        this.max_discount_amount = max_discount_amount;
        this.total_quantity = total_quantity;
        this.remain_quantity = remain_quantity;
        this.expired_at = expired_at;
    }

    public static Coupon create(String name, CouponType discount_type, Long discount_value,
        int min_amount, Long max_discount_amount, Long total_quantity, Long remain_quantity,
        LocalDateTime expired_at) {
        
        validateDiscountType(discount_type, min_amount, max_discount_amount, discount_value);
        validateExpiredAt(expired_at);
        
        return new Coupon(name, discount_type, discount_value, min_amount, max_discount_amount,
            total_quantity, remain_quantity, expired_at);
    }

    private static void validateDiscountType(CouponType discountType, int minAmount, Long maxDiscountAmount, Long discountValue) {
        if (discountType == CouponType.FIXED) {
            if (minAmount != 0) {
                throw new BusinessException(INVALID_COUPON_DISCOUNT_TYPE);
            }
            if (!maxDiscountAmount.equals(discountValue)) {
                throw new BusinessException(INVALID_COUPON_DISCOUNT_TYPE);
            }
        }

        if(discountType == CouponType.RATE) {
            if(minAmount <= 0 ){
                throw new BusinessException(INVALID_COUPON_DISCOUNT_TYPE);
            }


        }
    }

    public static void validateExpiredAt(LocalDateTime expiredAt) {
        if (expiredAt.isBefore(LocalDateTime.now())) {
            throw new BusinessException(INVALID_COUPON_EXPIRED_AT);
        }
    }

    public void useStock() {
        if (this.remain_quantity <= 0) {
            throw new BusinessException(COUPON_OUT_OF_STOCK);
        }
        this.remain_quantity -= 1;
    }


    public void updateCoupon(String couponName, LocalDateTime expiredAt) {
        if (couponName != null) {
            this.name = couponName;
        }

        if (expiredAt != null) {
            validateExpiredAt(expiredAt);
            this.expired_at = expiredAt;
        }
    }

}
