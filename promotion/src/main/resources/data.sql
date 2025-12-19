-- 등급 정보
INSERT INTO p_grades (grade_id, grade_name, benefit_discount_rate, min_amount, max_amount, created_at, updated_at)
VALUES
    (gen_random_uuid(), '등급 없음', 0, 0, NULL, NOW(), NOW()),
    (gen_random_uuid(), 'NONE', 0, 0, NULL, NOW(), NOW()),
    (gen_random_uuid(), 'BRONZE', 4, 1000001, 5000000, NOW(), NOW()),
    (gen_random_uuid(), 'SILVER', 7, 5000001, 10000000, NOW(), NOW()),
    (gen_random_uuid(), 'GOLD', 10, 10000001, 1000000000, NOW(), NOW())
ON CONFLICT (grade_name) DO NOTHING;

-- 쿠폰 정보
-- CouponType enum: FIXED | RATE
INSERT INTO p_coupons (
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
    updated_at
)
SELECT
    gen_random_uuid(),
    '신규 회원가입 쿠폰',
    'RATE',
    10,
    1,
    30000,
    100000000,
    10000,
    NOW() + INTERVAL '1 year',
    NOW(),
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM p_coupons WHERE name = '신규 회원가입 쿠폰')
ON CONFLICT DO NOTHING;
