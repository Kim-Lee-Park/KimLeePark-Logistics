-- prerequisites:
-- 1) hub/src/main/resources/data.sql 로 허브/업체/상품/재고를 먼저 생성
-- 2) 사용자 DB에 마스터 유저(user_id=1) 생성
-- 3) orders DB에서 실행

WITH base AS (
    SELECT
        (SELECT company_id FROM p_companies WHERE type = 'SUPPLIER' ORDER BY company_id LIMIT 1) AS supplier_id,
        (SELECT product_id FROM p_products ORDER BY product_id LIMIT 1) AS product_id,
        (SELECT name FROM p_products ORDER BY product_id LIMIT 1) AS product_name,
        (SELECT hub_id FROM p_hubs ORDER BY hub_id LIMIT 1) AS hub_id,
        (SELECT latitude FROM p_hubs ORDER BY hub_id LIMIT 1) AS lat,
        (SELECT longitude FROM p_hubs ORDER BY hub_id LIMIT 1) AS lng
),
orders AS (
    INSERT INTO p_orders (
        order_id,
        user_id,
        user_coupon_id,
        supplier_id,
        comment,
        order_status,
        original_price,
        coupon_discount_price,
        grade_discount_price,
        order_price,
        address_id,
        delivery_latitude,
        delivery_longitude,
        created_at,
        updated_at,
        created_by,
        updated_by
    )
    SELECT
        gen_random_uuid(),
        1, -- master 사용자
        NULL,
        base.supplier_id,
        CONCAT('load-test order #', gs),
        'PENDING',
        10000,
        0,
        0,
        10000,
        gen_random_uuid(),
        base.lat,
        base.lng,
        NOW(),
        NOW(),
        1,
        1
    FROM generate_series(1, 100000) AS gs
    CROSS JOIN base
    WHERE base.supplier_id IS NOT NULL
      AND base.product_id IS NOT NULL
      AND base.hub_id IS NOT NULL
      AND base.lat IS NOT NULL
      AND base.lng IS NOT NULL
    RETURNING order_id
)
INSERT INTO p_order_items (
    order_item_id,
    order_id,
    product_id,
    product_name,
    price,
    hub_id,
    quantity,
    created_at,
    updated_at,
    created_by,
    updated_by
)
SELECT
    gen_random_uuid(),
    o.order_id,
    base.product_id,
    base.product_name,
    10000,
    base.hub_id,
    1,
    NOW(),
    NOW(),
    1,
    1
FROM orders o
CROSS JOIN base
WHERE base.product_id IS NOT NULL
  AND base.hub_id IS NOT NULL;
