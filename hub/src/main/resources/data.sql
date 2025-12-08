INSERT INTO hub_schema.p_hubs (hub_id, name, latitude, longitude, address, status, created_at)
VALUES
(gen_random_uuid(), '서울특별시 센터', 37.474204655, 127.123625220, '서울특별시 송파구 송파대로 55', 'ACTIVE', NOW()),
(gen_random_uuid(), '경기 북부 센터', 37.640371630, 126.873795146, '경기도 고양시 덕양구 권율대로 570', 'ACTIVE', NOW()),
(gen_random_uuid(), '경기 남부 센터', 37.189621328, 127.375050054, '경기도 이천시 덕평로 257-21', 'ACTIVE', NOW()),
(gen_random_uuid(), '부산광역시 센터', 35.117003755, 129.042673080, '부산광역시 동구 중앙대로 206', 'ACTIVE', NOW()),
(gen_random_uuid(), '대구광역시 센터', 35.876025477, 128.596081221, '대구광역시 북구 태평로 161', 'ACTIVE', NOW()),
(gen_random_uuid(), '인천광역시 센터', 37.456317223, 126.704380115, '인천광역시 남동구 정각로 29', 'ACTIVE', NOW()),
(gen_random_uuid(), '광주광역시 센터', 35.160128610, 126.851451621, '광주광역시 서구 내방로 111', 'ACTIVE', NOW()),
(gen_random_uuid(), '대전광역시 센터', 36.350290100, 127.384984276, '대전광역시 서구 둔산로 100', 'ACTIVE', NOW()),
(gen_random_uuid(), '울산광역시 센터', 35.539590906, 129.311548261, '울산광역시 남구 중앙로 201', 'ACTIVE', NOW()),
(gen_random_uuid(), '세종특별자치시 센터', 36.480112199, 127.289097137, '세종특별자치시 한누리대로 2130', 'ACTIVE', NOW()),
(gen_random_uuid(), '강원특별자치도 센터', 37.885424118, 127.729638401, '강원특별자치도 춘천시 중앙로 1', 'ACTIVE', NOW()),
(gen_random_uuid(), '충청북도 센터', 36.635384936, 127.491459079, '충청북도 청주시 상당구 상당로 82', 'ACTIVE', NOW()),
(gen_random_uuid(), '충청남도 센터', 36.659041812, 126.673059194, '충청남도 홍성군 홍북읍 충남대로 21', 'ACTIVE', NOW()),
(gen_random_uuid(), '전북특별자치도 센터', 35.820421866, 127.108665314, '전북특별자치도 전주시 완산구 효자로 225', 'ACTIVE', NOW()),
(gen_random_uuid(), '전라남도 센터', 34.816180813, 126.462867569, '전라남도 무안군 삼향읍 오룡길 1', 'ACTIVE', NOW()),
(gen_random_uuid(), '경상북도 센터', 36.576110086, 128.505679615, '경상북도 안동시 풍천면 도청대로 455', 'ACTIVE', NOW()),
(gen_random_uuid(), '경상남도 센터', 35.238089648, 128.692343711, '경상남도 창원시 의창구 중앙대로 300', 'ACTIVE', NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO hub_schema.p_companies (company_id, hub_id, type, name, address, created_at)
SELECT gen_random_uuid(), (SELECT hub_id FROM hub_schema.p_hubs WHERE name = '서울특별시 센터'), 'SUPPLIER', '서울 반도체 협력사', '서울특별시 금천구 가산디지털1로 30', NOW()
WHERE NOT EXISTS (SELECT 1 FROM hub_schema.p_companies WHERE name = '서울 반도체 협력사');

INSERT INTO hub_schema.p_companies (company_id, hub_id, type, name, address, created_at)
SELECT gen_random_uuid(), (SELECT hub_id FROM hub_schema.p_hubs WHERE name = '경기 남부 센터'), 'CUSTOMER', '용인 가전 리테일', '경기도 용인시 기흥구 흥덕중앙로 120', NOW()
WHERE NOT EXISTS (SELECT 1 FROM hub_schema.p_companies WHERE name = '용인 가전 리테일');

INSERT INTO hub_schema.p_companies (company_id, hub_id, type, name, address, created_at)
SELECT gen_random_uuid(), (SELECT hub_id FROM hub_schema.p_hubs WHERE name = '부산광역시 센터'), 'SUPPLIER', '부산 해운 포장재', '부산광역시 사하구 감천항로 45', NOW()
WHERE NOT EXISTS (SELECT 1 FROM hub_schema.p_companies WHERE name = '부산 해운 포장재');

INSERT INTO hub_schema.p_companies (company_id, hub_id, type, name, address, created_at)
SELECT gen_random_uuid(), (SELECT hub_id FROM hub_schema.p_hubs WHERE name = '대구광역시 센터'), 'CUSTOMER', '대구 식자재 납품처', '대구광역시 달서구 달서대로 109', NOW()
WHERE NOT EXISTS (SELECT 1 FROM hub_schema.p_companies WHERE name = '대구 식자재 납품처');

INSERT INTO hub_schema.p_companies (company_id, hub_id, type, name, address, created_at)
SELECT gen_random_uuid(), (SELECT hub_id FROM hub_schema.p_hubs WHERE name = '인천광역시 센터'), 'CUSTOMER', '인천 공항 면세 물류', '인천광역시 중구 공항로 272', NOW()
WHERE NOT EXISTS (SELECT 1 FROM hub_schema.p_companies WHERE name = '인천 공항 면세 물류');

INSERT INTO hub_schema.p_products (product_id, company_id, name, created_at)
VALUES
(gen_random_uuid(), (SELECT company_id FROM hub_schema.p_companies WHERE name = '서울 반도체 협력사' ORDER BY company_id LIMIT 1), 'LED 모듈 세트', NOW()),
(gen_random_uuid(), (SELECT company_id FROM hub_schema.p_companies WHERE name = '용인 가전 리테일' ORDER BY company_id LIMIT 1), '스마트TV 패널', NOW()),
(gen_random_uuid(), (SELECT company_id FROM hub_schema.p_companies WHERE name = '부산 해운 포장재' ORDER BY company_id LIMIT 1), '선적용 포장 박스', NOW()),
(gen_random_uuid(), (SELECT company_id FROM hub_schema.p_companies WHERE name = '대구 식자재 납품처' ORDER BY company_id LIMIT 1), '냉동 수산 세트', NOW()),
(gen_random_uuid(), (SELECT company_id FROM hub_schema.p_companies WHERE name = '인천 공항 면세 물류' ORDER BY company_id LIMIT 1), '면세 화장품 키트', NOW())
ON CONFLICT DO NOTHING;

INSERT INTO hub_schema.p_inventory (inventory_id, quantity, product_id, hub_id, created_at)
VALUES
(gen_random_uuid(), 120, (SELECT product_id FROM hub_schema.p_products WHERE name = 'LED 모듈 세트' ORDER BY product_id LIMIT 1), (SELECT hub_id FROM hub_schema.p_hubs WHERE name = '서울특별시 센터' ORDER BY hub_id LIMIT 1), NOW()),
(gen_random_uuid(), 75, (SELECT product_id FROM hub_schema.p_products WHERE name = '스마트TV 패널' ORDER BY product_id LIMIT 1), (SELECT hub_id FROM hub_schema.p_hubs WHERE name = '경기 남부 센터' ORDER BY hub_id LIMIT 1), NOW()),
(gen_random_uuid(), 200, (SELECT product_id FROM hub_schema.p_products WHERE name = '선적용 포장 박스' ORDER BY product_id LIMIT 1), (SELECT hub_id FROM hub_schema.p_hubs WHERE name = '부산광역시 센터' ORDER BY hub_id LIMIT 1), NOW()),
(gen_random_uuid(), 90, (SELECT product_id FROM hub_schema.p_products WHERE name = '냉동 수산 세트' ORDER BY product_id LIMIT 1), (SELECT hub_id FROM hub_schema.p_hubs WHERE name = '대구광역시 센터' ORDER BY hub_id LIMIT 1), NOW()),
(gen_random_uuid(), 60, (SELECT product_id FROM hub_schema.p_products WHERE name = '면세 화장품 키트' ORDER BY product_id LIMIT 1), (SELECT hub_id FROM hub_schema.p_hubs WHERE name = '인천광역시 센터' ORDER BY hub_id LIMIT 1), NOW())
ON CONFLICT (product_id, hub_id) DO NOTHING;
