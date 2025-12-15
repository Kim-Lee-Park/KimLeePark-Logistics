-- 테스트용 상품/재고를 생성하거나 수량을 초기화합니다.
-- 실행 대상 DB: hub
-- 재고 수량은 환경에 맞게 quantity 값을 원하는 테스트 건수로 조정하세요.

WITH base_hub AS (
    SELECT hub_id FROM p_hubs ORDER BY hub_id LIMIT 1
),
supplier AS (
    SELECT company_id, hub_id FROM p_companies WHERE type = 'SUPPLIER' ORDER BY company_id LIMIT 1
),
upsert_product AS (
    INSERT INTO p_products (product_id, company_id, name, created_at)
    SELECT gen_random_uuid(), supplier.company_id, 'load-test-product', NOW()
    FROM supplier
    ON CONFLICT (name) DO NOTHING
    RETURNING product_id, company_id
),
product AS (
    SELECT
        COALESCE(upsert_product.product_id,
                 (SELECT product_id FROM p_products WHERE name = 'load-test-product' ORDER BY product_id LIMIT 1)
        ) AS product_id,
        COALESCE(upsert_product.company_id,
                 (SELECT company_id FROM p_products WHERE name = 'load-test-product' ORDER BY product_id LIMIT 1)
        ) AS company_id
    FROM upsert_product
    UNION ALL
    SELECT
        (SELECT product_id FROM p_products WHERE name = 'load-test-product' ORDER BY product_id LIMIT 1),
        (SELECT company_id FROM p_products WHERE name = 'load-test-product' ORDER BY product_id LIMIT 1)
    WHERE NOT EXISTS (SELECT 1 FROM upsert_product)
)
INSERT INTO p_inventory (inventory_id, quantity, product_id, hub_id, created_at)
SELECT gen_random_uuid(), 100, product.product_id, COALESCE(supplier.hub_id, base_hub.hub_id), NOW()
FROM product, supplier, base_hub
ON CONFLICT (product_id, hub_id) DO UPDATE
    SET quantity = EXCLUDED.quantity;

-- 조회용: 상품/재고 ID 확인
SELECT 'product_id' AS key, product_id AS value FROM product;
SELECT 'hub_id' AS key, COALESCE(supplier.hub_id, base_hub.hub_id) AS value FROM supplier, base_hub LIMIT 1;
