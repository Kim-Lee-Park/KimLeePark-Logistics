-- 1. 쿠폰 생성 (정액 10,000원 할인)
INSERT INTO promotion_schema.p_coupons (
    coupon_id,
    name,
    discount_type,
    discount_value,
    min_amount,
    max_discount_amount,
    total_quantity,
    remain_quantity,
    expired_at,
    created_at,
    created_by
) VALUES (
             '550e8400-e29b-41d4-a716-446655440029'::uuid,
             '테스트 10,000원 할인 쿠폰',
             'FIXED',
             10000,
             0,
             10000,
             100,
             100,
             '2025-12-31 23:59:59',
             NOW(),
             1
         );

-- 2. 사용자 쿠폰 발급 (userId: 1)
INSERT INTO promotion_schema.p_user_coupons (
    user_coupon_id,
    coupon_id,
    user_id,
    status,
    created_at,
    created_by
) VALUES (
             '550e8400-e29b-41d4-a716-446655440030'::uuid,
             '550e8400-e29b-41d4-a716-446655440029'::uuid,
             1,
             'READY',
             NOW(),
             1
         );