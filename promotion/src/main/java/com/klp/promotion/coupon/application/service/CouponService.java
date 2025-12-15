package com.klp.promotion.coupon.application.service;

import static com.klp.promotion.coupon.common.exception.CouponErrorCode.COUPON_DELETE_NOT_ALLOWED;
import static com.klp.promotion.coupon.common.exception.CouponErrorCode.COUPON_NOT_FOUND;

import com.klp.promotion.coupon.application.command.CouponCommand;
import com.klp.promotion.coupon.domain.entity.Coupon;
import com.klp.promotion.coupon.domain.repository.CouponRepository;
import com.klp.promotion.coupon.presentation.dto.CouponDetailResponse;
import com.klp.promotion.coupon.presentation.dto.CouponResponse;
import com.klp.promotion.coupon.presentation.dto.CreateCouponRequest;
import com.klp.promotion.global.exception.BusinessException;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    public boolean decreaseStock(UUID couponId) {

        return couponRepository.decreaseStock(couponId);

    }

    @Transactional
    public Coupon updateStock(UUID couponId) {
        Coupon coupon = findByCouponId(couponId);
        coupon.useStock();
        return coupon;

    }

    @Transactional(readOnly = true)
    public Coupon findByCouponId(UUID couponId) {
        return couponRepository.findByCouponId(couponId);
    }

    @Transactional
    public CouponResponse createCoupon(CreateCouponRequest request) {
        CouponCommand command = request.toCommand();

        Coupon coupon = Coupon.create(
            command.name(),
            command.discount_type(),
            command.discount_value(),
            command.min_amount(),
            command.max_discount_amount(),
            command.total_quantity(),
            command.total_quantity(), // 초기에는 remain_quantity = total_quantity
            command.expired_at()
        );

        Coupon result = couponRepository.save(coupon);
        
        // Redis에 재고 저장
        couponRepository.saveStockToRedis(result.getCouponId(), result.getRemain_quantity());

        return new CouponResponse(result.getCouponId());
    }

    @Transactional
    public void updateCoupon(UUID couponId, String name, LocalDateTime expiredAt) {
        Coupon coupon = findByCouponId(couponId);
        if (coupon == null) {
            throw new BusinessException(COUPON_NOT_FOUND);
        }

        coupon.updateCoupon(name, expiredAt);
    }

    @Transactional
    public void deleteCoupon(UUID couponId, Long userId) {
        Coupon coupon = findByCouponId(couponId);
        if (coupon == null || coupon.isDeleted()) {
            throw new BusinessException(COUPON_NOT_FOUND);
        }

        if (coupon.getTotal_quantity() != coupon.getRemain_quantity()) {
            throw new BusinessException(COUPON_DELETE_NOT_ALLOWED);
        }

        coupon.delete(userId);
    }

    @Transactional(readOnly = true)
    public Page<CouponDetailResponse> findCoupons(Pageable pageable) {
        Page<Coupon> couponPage = couponRepository.findAll(pageable);
        return couponPage.map(CouponDetailResponse::from);
    }
}
