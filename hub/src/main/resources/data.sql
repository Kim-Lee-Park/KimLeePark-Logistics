-- 허브 메타 데이터
INSERT INTO p_hubs (hub_id, name, latitude, longitude, address, status, created_at)
VALUES ('aaaaaaaa-0000-0000-0002-000000000001', '서울특별시 센터', 37.474204655, 127.123625220, '서울특별시 송파구 송파대로 55', 'ACTIVE',
        NOW()),
       ('aaaaaaaa-0000-0000-0002-000000000002', '경기 북부 센터', 37.640371630, 126.873795146, '경기도 고양시 덕양구 권율대로 570',
        'ACTIVE', NOW()),
       ('aaaaaaaa-0000-0000-0002-000000000003', '경기 남부 센터', 37.189621328, 127.375050054, '경기도 이천시 덕평로 257-21', 'ACTIVE',
        NOW()),
       ('aaaaaaaa-0000-0000-0002-000000000004', '부산광역시 센터', 35.117003755, 129.042673080, '부산광역시 동구 중앙대로 206', 'ACTIVE',
        NOW()),
       ('aaaaaaaa-0000-0000-0002-000000000005', '대구광역시 센터', 35.876025477, 128.596081221, '대구광역시 북구 태평로 161', 'ACTIVE',
        NOW()),
       ('aaaaaaaa-0000-0000-0002-000000000006', '인천광역시 센터', 37.456317223, 126.704380115, '인천광역시 남동구 정각로 29', 'ACTIVE',
        NOW()),
       ('aaaaaaaa-0000-0000-0002-000000000007', '광주광역시 센터', 35.160128610, 126.851451621, '광주광역시 서구 내방로 111', 'ACTIVE',
        NOW()),
       ('aaaaaaaa-0000-0000-0002-000000000008', '대전광역시 센터', 36.350290100, 127.384984276, '대전광역시 서구 둔산로 100', 'ACTIVE',
        NOW()),
       ('aaaaaaaa-0000-0000-0002-000000000009', '울산광역시 센터', 35.539590906, 129.311548261, '울산광역시 남구 중앙로 201', 'ACTIVE',
        NOW()),
       ('aaaaaaaa-0000-0000-0002-000000000010', '세종특별자치시 센터', 36.480112199, 127.289097137, '세종특별자치시 한누리대로 2130',
        'ACTIVE', NOW()),
       ('aaaaaaaa-0000-0000-0002-000000000011', '강원특별자치도 센터', 37.885424118, 127.729638401, '강원특별자치도 춘천시 중앙로 1',
        'ACTIVE', NOW()),
       ('aaaaaaaa-0000-0000-0002-000000000012', '충청북도 센터', 36.635384936, 127.491459079, '충청북도 청주시 상당구 상당로 82', 'ACTIVE',
        NOW()),
       ('aaaaaaaa-0000-0000-0002-000000000013', '충청남도 센터', 36.659041812, 126.673059194, '충청남도 홍성군 홍북읍 충남대로 21',
        'ACTIVE', NOW()),
       ('aaaaaaaa-0000-0000-0002-000000000014', '전북특별자치도 센터', 35.820421866, 127.108665314, '전북특별자치도 전주시 완산구 효자로 225',
        'ACTIVE', NOW()),
       ('aaaaaaaa-0000-0000-0002-000000000015', '전라남도 센터', 34.816180813, 126.462867569, '전라남도 무안군 삼향읍 오룡길 1', 'ACTIVE',
        NOW()),
       ('aaaaaaaa-0000-0000-0002-000000000016', '경상북도 센터', 36.576110086, 128.505679615, '경상북도 안동시 풍천면 도청대로 455',
        'ACTIVE', NOW()),
       ('aaaaaaaa-0000-0000-0002-000000000017', '경상남도 센터', 35.238089648, 128.692343711, '경상남도 창원시 의창구 중앙대로 300',
        'ACTIVE', NOW())
ON CONFLICT (name) DO NOTHING;

-- 업체 데이터 생성 (34개 업체, 17개 허브에 각 2개씩)
INSERT INTO p_companies (company_id, hub_id, type, name, address, created_at, updated_at)
VALUES
    -- 서울 센터 (2개)
    ('bbbbbbbb-0000-0000-0002-000000000001', 'aaaaaaaa-0000-0000-0002-000000000001', 'SUPPLIER', '삼성전자',
     '서울특별시 강남구 삼성로 123', NOW(), NOW()),
    ('bbbbbbbb-0000-0000-0002-000000000002', 'aaaaaaaa-0000-0000-0002-000000000001', 'SUPPLIER', '애플코리아',
     '서울특별시 강남구 테헤란로 456', NOW(), NOW()),
    -- 경기 북부 센터 (2개)
    ('bbbbbbbb-0000-0000-0002-000000000003', 'aaaaaaaa-0000-0000-0002-000000000002', 'SUPPLIER', 'LG전자',
     '경기도 고양시 일산동구 중앙로 100', NOW(), NOW()),
    ('bbbbbbbb-0000-0000-0002-000000000004', 'aaaaaaaa-0000-0000-0002-000000000002', 'SUPPLIER', '현대리바트',
     '경기도 고양시 덕양구 화신로 200', NOW(), NOW()),
    -- 경기 남부 센터 (2개)
    ('bbbbbbbb-0000-0000-0002-000000000005', 'aaaaaaaa-0000-0000-0002-000000000003', 'SUPPLIER', '쿠쿠전자',
     '경기도 이천시 부발읍 경충대로 300', NOW(), NOW()),
    ('bbbbbbbb-0000-0000-0002-000000000006', 'aaaaaaaa-0000-0000-0002-000000000003', 'SUPPLIER', '코웨이',
     '경기도 용인시 기흥구 흥덕로 400', NOW(), NOW()),
    -- 부산 센터 (2개)
    ('bbbbbbbb-0000-0000-0002-000000000007', 'aaaaaaaa-0000-0000-0002-000000000004', 'SUPPLIER', '르노코리아',
     '부산광역시 강서구 녹산산업로 500', NOW(), NOW()),
    ('bbbbbbbb-0000-0000-0002-000000000008', 'aaaaaaaa-0000-0000-0002-000000000004', 'SUPPLIER', '동원F&B',
     '부산광역시 해운대구 센텀로 600', NOW(), NOW()),
    -- 대구 센터 (2개)
    ('bbbbbbbb-0000-0000-0002-000000000009', 'aaaaaaaa-0000-0000-0002-000000000005', 'SUPPLIER', '삼성SDI',
     '대구광역시 달성군 현풍읍 테크노로 700', NOW(), NOW()),
    ('bbbbbbbb-0000-0000-0002-000000000010', 'aaaaaaaa-0000-0000-0002-000000000005', 'SUPPLIER', '코오롱인더스트리',
     '대구광역시 북구 침산로 800', NOW(), NOW()),
    -- 인천 센터 (2개)
    ('bbbbbbbb-0000-0000-0002-000000000011', 'aaaaaaaa-0000-0000-0002-000000000006', 'SUPPLIER', 'SK하이닉스',
     '인천광역시 연수구 송도과학로 900', NOW(), NOW()),
    ('bbbbbbbb-0000-0000-0002-000000000012', 'aaaaaaaa-0000-0000-0002-000000000006', 'SUPPLIER', '한진',
     '인천광역시 중구 공항로 1000', NOW(), NOW()),
    -- 광주 센터 (2개)
    ('bbbbbbbb-0000-0000-0002-000000000013', 'aaaaaaaa-0000-0000-0002-000000000007', 'SUPPLIER', '기아자동차',
     '광주광역시 서구 상무대로 1100', NOW(), NOW()),
    ('bbbbbbbb-0000-0000-0002-000000000014', 'aaaaaaaa-0000-0000-0002-000000000007', 'SUPPLIER', '금호타이어',
     '광주광역시 광산구 첨단과기로 1200', NOW(), NOW()),
    -- 대전 센터 (2개)
    ('bbbbbbbb-0000-0000-0002-000000000015', 'aaaaaaaa-0000-0000-0002-000000000008', 'SUPPLIER', '한화에어로스페이스',
     '대전광역시 유성구 대덕대로 1300', NOW(), NOW()),
    ('bbbbbbbb-0000-0000-0002-000000000016', 'aaaaaaaa-0000-0000-0002-000000000008', 'SUPPLIER', '로지텍코리아',
     '대전광역시 유성구 대학로 1400', NOW(), NOW()),
    -- 울산 센터 (2개)
    ('bbbbbbbb-0000-0000-0002-000000000017', 'aaaaaaaa-0000-0000-0002-000000000009', 'SUPPLIER', '현대자동차',
     '울산광역시 북구 명촌로 1500', NOW(), NOW()),
    ('bbbbbbbb-0000-0000-0002-000000000018', 'aaaaaaaa-0000-0000-0002-000000000009', 'SUPPLIER', 'SK에너지',
     '울산광역시 남구 처용로 1600', NOW(), NOW()),
    -- 세종 센터 (2개)
    ('bbbbbbbb-0000-0000-0002-000000000019', 'aaaaaaaa-0000-0000-0002-000000000010', 'SUPPLIER', '오뚜기',
     '세종특별자치시 연서면 산업단지로 1700', NOW(), NOW()),
    ('bbbbbbbb-0000-0000-0002-000000000020', 'aaaaaaaa-0000-0000-0002-000000000010', 'SUPPLIER', '풀무원',
     '세종특별자치시 전의면 공단로 1800', NOW(), NOW()),
    -- 강원 센터 (2개)
    ('bbbbbbbb-0000-0000-0002-000000000021', 'aaaaaaaa-0000-0000-0002-000000000011', 'SUPPLIER', '하이트진로',
     '강원특별자치도 춘천시 동면 만천로 1900', NOW(), NOW()),
    ('bbbbbbbb-0000-0000-0002-000000000022', 'aaaaaaaa-0000-0000-0002-000000000011', 'SUPPLIER', '농심',
     '강원특별자치도 원주시 문막읍 문막공단로 2000', NOW(), NOW()),
    -- 충북 센터 (2개)
    ('bbbbbbbb-0000-0000-0002-000000000023', 'aaaaaaaa-0000-0000-0002-000000000012', 'SUPPLIER', 'SK바이오팜',
     '충청북도 청주시 흥덕구 오송읍 오송생명로 2100', NOW(), NOW()),
    ('bbbbbbbb-0000-0000-0002-000000000024', 'aaaaaaaa-0000-0000-0002-000000000012', 'SUPPLIER', '한국타이어',
     '충청북도 청주시 서원구 남이면 산업단지로 2200', NOW(), NOW()),
    -- 충남 센터 (2개)
    ('bbbbbbbb-0000-0000-0002-000000000025', 'aaaaaaaa-0000-0000-0002-000000000013', 'SUPPLIER', '삼성디스플레이',
     '충청남도 아산시 탕정면 삼성로 2300', NOW(), NOW()),
    ('bbbbbbbb-0000-0000-0002-000000000026', 'aaaaaaaa-0000-0000-0002-000000000013', 'SUPPLIER', '현대제철',
     '충청남도 당진시 송악읍 현대로 2400', NOW(), NOW()),
    -- 전북 센터 (2개)
    ('bbbbbbbb-0000-0000-0002-000000000027', 'aaaaaaaa-0000-0000-0002-000000000014', 'SUPPLIER', '현대모비스',
     '전북특별자치도 전주시 덕진구 혁신로 2500', NOW(), NOW()),
    ('bbbbbbbb-0000-0000-0002-000000000028', 'aaaaaaaa-0000-0000-0002-000000000014', 'SUPPLIER', '삼양식품',
     '전북특별자치도 익산시 왕궁면 삼양로 2600', NOW(), NOW()),
    -- 전남 센터 (2개)
    ('bbbbbbbb-0000-0000-0002-000000000029', 'aaaaaaaa-0000-0000-0002-000000000015', 'SUPPLIER', '포스코케미칼',
     '전라남도 광양시 광양읍 포스코대로 2700', NOW(), NOW()),
    ('bbbbbbbb-0000-0000-0002-000000000030', 'aaaaaaaa-0000-0000-0002-000000000015', 'SUPPLIER', 'GS칼텍스',
     '전라남도 여수시 여수산단로 2800', NOW(), NOW()),
    -- 경북 센터 (2개)
    ('bbbbbbbb-0000-0000-0002-000000000031', 'aaaaaaaa-0000-0000-0002-000000000016', 'SUPPLIER', '포스코',
     '경상북도 포항시 남구 청암로 2900', NOW(), NOW()),
    ('bbbbbbbb-0000-0000-0002-000000000032', 'aaaaaaaa-0000-0000-0002-000000000016', 'SUPPLIER', '삼성전기',
     '경상북도 구미시 산동읍 첨단기업로 3000', NOW(), NOW()),
    -- 경남 센터 (2개)
    ('bbbbbbbb-0000-0000-0002-000000000033', 'aaaaaaaa-0000-0000-0002-000000000017', 'SUPPLIER', '한국항공우주',
     '경상남도 사천시 사남면 공단로 3100', NOW(), NOW()),
    ('bbbbbbbb-0000-0000-0002-000000000034', 'aaaaaaaa-0000-0000-0002-000000000017', 'SUPPLIER', '두산중공업',
     '경상남도 창원시 성산구 두산볼보로 3200', NOW(), NOW())
ON CONFLICT (company_id) DO NOTHING;

-- 상품 데이터(1만 개)
INSERT INTO p_products (product_id, company_id, name, category, created_at, updated_at)
SELECT ('bbbbbbbb-0000-0000-0000-' || LPAD(num::TEXT, 12, '0'))::UUID AS product_id,
       company_id,
       product_name,
       category,
       NOW()                                                          AS created_at,
       NOW()                                                          AS updated_at
FROM (SELECT num,
             -- 업체를 순환하며 할당 (34개 업체)
             CASE (num - 1) % 34
                 WHEN 0 THEN 'cccccccc-0000-0000-0002-000000000001'::UUID -- 삼성전자
                 WHEN 1 THEN 'cccccccc-0000-0000-0002-000000000002'::UUID -- 애플코리아
                 WHEN 2 THEN 'cccccccc-0000-0000-0002-000000000003'::UUID -- LG전자
                 WHEN 3 THEN 'cccccccc-0000-0000-0002-000000000004'::UUID -- 현대리바트
                 WHEN 4 THEN 'cccccccc-0000-0000-0002-000000000005'::UUID -- 쿠쿠전자
                 WHEN 5 THEN 'cccccccc-0000-0000-0002-000000000006'::UUID -- 코웨이
                 WHEN 6 THEN 'cccccccc-0000-0000-0002-000000000007'::UUID -- 르노코리아
                 WHEN 7 THEN 'cccccccc-0000-0000-0002-000000000008'::UUID -- 동원F&B
                 WHEN 8 THEN 'cccccccc-0000-0000-0002-000000000009'::UUID -- 삼성SDI
                 WHEN 9 THEN 'cccccccc-0000-0000-0002-000000000010'::UUID -- 코오롱인더스트리
                 WHEN 10 THEN 'cccccccc-0000-0000-0002-000000000011'::UUID -- SK하이닉스
                 WHEN 11 THEN 'cccccccc-0000-0000-0002-000000000012'::UUID -- 한진
                 WHEN 12 THEN 'cccccccc-0000-0000-0002-000000000013'::UUID -- 기아자동차
                 WHEN 13 THEN 'cccccccc-0000-0000-0002-000000000014'::UUID -- 금호타이어
                 WHEN 14 THEN 'cccccccc-0000-0000-0002-000000000015'::UUID -- 한화에어로스페이스
                 WHEN 15 THEN 'cccccccc-0000-0000-0002-000000000016'::UUID -- 로지텍코리아
                 WHEN 16 THEN 'cccccccc-0000-0000-0002-000000000017'::UUID -- 현대자동차
                 WHEN 17 THEN 'cccccccc-0000-0000-0002-000000000018'::UUID -- SK에너지
                 WHEN 18 THEN 'cccccccc-0000-0000-0002-000000000019'::UUID -- 오뚜기
                 WHEN 19 THEN 'cccccccc-0000-0000-0002-000000000020'::UUID -- 풀무원
                 WHEN 20 THEN 'cccccccc-0000-0000-0002-000000000021'::UUID -- 하이트진로
                 WHEN 21 THEN 'cccccccc-0000-0000-0002-000000000022'::UUID -- 농심
                 WHEN 22 THEN 'cccccccc-0000-0000-0002-000000000023'::UUID -- SK바이오팜
                 WHEN 23 THEN 'cccccccc-0000-0000-0002-000000000024'::UUID -- 한국타이어
                 WHEN 24 THEN 'cccccccc-0000-0000-0002-000000000025'::UUID -- 삼성디스플레이
                 WHEN 25 THEN 'cccccccc-0000-0000-0002-000000000026'::UUID -- 현대제철
                 WHEN 26 THEN 'cccccccc-0000-0000-0002-000000000027'::UUID -- 현대모비스
                 WHEN 27 THEN 'cccccccc-0000-0000-0002-000000000028'::UUID -- 삼양식품
                 WHEN 28 THEN 'cccccccc-0000-0000-0002-000000000029'::UUID -- 포스코케미칼
                 WHEN 29 THEN 'cccccccc-0000-0000-0002-000000000030'::UUID -- GS칼텍스
                 WHEN 30 THEN 'cccccccc-0000-0000-0002-000000000031'::UUID -- 포스코
                 WHEN 31 THEN 'cccccccc-0000-0000-0002-000000000032'::UUID -- 삼성전기
                 WHEN 32 THEN 'cccccccc-0000-0000-0002-000000000033'::UUID -- 한국항공우주
                 WHEN 33 THEN 'cccccccc-0000-0000-0002-000000000034'::UUID -- 두산중공업
                 END AS company_id,
             -- 업체별 상품명 및 카테고리
             CASE (num - 1) % 34
                 -- 삼성전자 - ELECTRONICS
                 WHEN 0 THEN
                     CASE ((num - 1) / 34) % 30
                         WHEN 0 THEN '갤럭시 S24 Ultra'
                         WHEN 1 THEN '갤럭시 S24 Plus'
                         WHEN 2 THEN '갤럭시 S24'
                         WHEN 3 THEN '갤럭시 Z Fold5'
                         WHEN 4 THEN '갤럭시 Z Flip5'
                         WHEN 5 THEN '갤럭시 A54'
                         WHEN 6 THEN '갤럭시 버즈3 Pro'
                         WHEN 7 THEN '갤럭시 버즈3'
                         WHEN 8 THEN '갤럭시 워치6 Classic'
                         WHEN 9 THEN '갤럭시 워치6'
                         WHEN 10 THEN '갤럭시 탭 S9 Ultra'
                         WHEN 11 THEN '갤럭시 탭 S9'
                         WHEN 12 THEN '갤럭시 북4 Pro'
                         WHEN 13 THEN 'QLED TV 85인치'
                         WHEN 14 THEN 'QLED TV 75인치'
                         WHEN 15 THEN 'QLED TV 65인치'
                         WHEN 16 THEN 'OLED TV 77인치'
                         WHEN 17 THEN 'Neo QLED 8K'
                         WHEN 18 THEN 'The Frame TV'
                         WHEN 19 THEN '게이밍 모니터 Odyssey'
                         WHEN 20 THEN '무선충전 패드'
                         WHEN 21 THEN '스마트 태그'
                         WHEN 22 THEN '갤럭시 S 펜'
                         WHEN 23 THEN '삼성 SSD 990 Pro'
                         WHEN 24 THEN '삼성 SSD 980'
                         WHEN 25 THEN '외장 SSD T7'
                         WHEN 26 THEN '메모리카드 Pro Plus'
                         WHEN 27 THEN 'USB 플래시드라이브'
                         WHEN 28 THEN '스마트 모니터 M8'
                         ELSE '갤럭시 제품 ' || ((num - 1) / 34)::TEXT || '호'
                         END || ' ' || ((num - 1) / 34)::TEXT || '호'
                 -- 애플코리아 - ELECTRONICS
                 WHEN 1 THEN
                     CASE ((num - 1) / 34) % 30
                         WHEN 0 THEN '아이폰 15 Pro Max'
                         WHEN 1 THEN '아이폰 15 Pro'
                         WHEN 2 THEN '아이폰 15 Plus'
                         WHEN 3 THEN '아이폰 15'
                         WHEN 4 THEN '아이폰 14 Pro'
                         WHEN 5 THEN '아이폰 SE'
                         WHEN 6 THEN '에어팟 Pro 2세대'
                         WHEN 7 THEN '에어팟 3세대'
                         WHEN 8 THEN '에어팟 Max'
                         WHEN 9 THEN '애플워치 Series 9'
                         WHEN 10 THEN '애플워치 Ultra 2'
                         WHEN 11 THEN '애플워치 SE'
                         WHEN 12 THEN '아이패드 Pro 12.9'
                         WHEN 13 THEN '아이패드 Pro 11'
                         WHEN 14 THEN '아이패드 Air'
                         WHEN 15 THEN '아이패드 mini'
                         WHEN 16 THEN '맥북 Pro 16인치 M3'
                         WHEN 17 THEN '맥북 Pro 14인치 M3'
                         WHEN 18 THEN '맥북 Air 15인치 M2'
                         WHEN 19 THEN '맥북 Air 13인치 M2'
                         WHEN 20 THEN '아이맥 24인치 M3'
                         WHEN 21 THEN '맥 미니 M2'
                         WHEN 22 THEN '맥 스튜디오 M2'
                         WHEN 23 THEN 'Studio Display'
                         WHEN 24 THEN '매직 키보드'
                         WHEN 25 THEN '매직 마우스'
                         WHEN 26 THEN '매직 트랙패드'
                         WHEN 27 THEN '애플 펜슬 2세대'
                         WHEN 28 THEN 'AirTag 4팩'
                         ELSE '애플 제품 ' || ((num - 1) / 34)::TEXT || '호'
                         END || ' ' || ((num - 1) / 34)::TEXT || '호'
                 -- LG전자 - ELECTRONICS, HOME_APPLIANCE
                 WHEN 2 THEN
                     CASE ((num - 1) / 34) % 30
                         WHEN 0 THEN 'LG 그램 17인치'
                         WHEN 1 THEN 'LG 그램 16인치'
                         WHEN 2 THEN 'LG 그램 15인치'
                         WHEN 3 THEN '톤프리 FP9'
                         WHEN 4 THEN '톤프리 T90'
                         WHEN 5 THEN 'OLED TV 83인치'
                         WHEN 6 THEN 'OLED TV 77인치'
                         WHEN 7 THEN 'QNED TV 86인치'
                         WHEN 8 THEN 'LG 스탠바이미'
                         WHEN 9 THEN 'UltraGear 모니터'
                         WHEN 10 THEN '트롬 건조기 21kg'
                         WHEN 11 THEN '트롬 세탁기 23kg'
                         WHEN 12 THEN '트롬 워시타워'
                         WHEN 13 THEN '코드제로 A9S'
                         WHEN 14 THEN '코드제로 M9'
                         WHEN 15 THEN '퓨리케어 360'
                         WHEN 16 THEN '퓨리케어 에어로타워'
                         WHEN 17 THEN '디오스 냉장고 870L'
                         WHEN 18 THEN '디오스 냉장고 682L'
                         WHEN 19 THEN '디오스 김치냉장고'
                         WHEN 20 THEN 'LG전자 제품 ' || ((num - 1) / 34)::TEXT || '호'
                         ELSE 'LG전자 제품 ' || ((num - 1) / 34)::TEXT || '호'
                         END || ' ' || ((num - 1) / 34)::TEXT || '호'
                 -- 현대리바트 - FURNITURE
                 WHEN 3 THEN '리바트 ' ||
                             CASE ((num - 1) / 34) % 20
                                 WHEN 0 THEN '이안 책상'
                                 WHEN 1 THEN '모아 서랍장'
                                 WHEN 2 THEN '라움 옷장'
                                 WHEN 3 THEN '스칸디 침대'
                                 WHEN 4 THEN '에코 소파'
                                 WHEN 5 THEN '아르떼 식탁'
                                 WHEN 6 THEN '베네 의자'
                                 WHEN 7 THEN '오피스 의자'
                                 WHEN 8 THEN '하임 신발장'
                                 WHEN 9 THEN '플로우 책장'
                                 WHEN 10 THEN '르엘 화장대'
                                 WHEN 11 THEN '시스템 선반'
                                 WHEN 12 THEN '리빙박스'
                                 WHEN 13 THEN '와이드 행거'
                                 WHEN 14 THEN '북스탠드'
                                 WHEN 15 THEN '콘솔 테이블'
                                 WHEN 16 THEN '벤치 의자'
                                 WHEN 17 THEN '매트리스'
                                 WHEN 18 THEN '러그'
                                 ELSE '가구'
                                 END || ' ' || ((num - 1) / 34)::TEXT || '호'
                 -- 쿠쿠전자 - KITCHEN_APPLIANCE
                 WHEN 4 THEN '쿠쿠 ' ||
                             CASE ((num - 1) / 34) % 15
                                 WHEN 0 THEN '트윈프레셔 밥솥'
                                 WHEN 1 THEN 'IH압력밥솥'
                                 WHEN 2 THEN '정수기'
                                 WHEN 3 THEN '에어프라이어'
                                 WHEN 4 THEN '전기레인지'
                                 WHEN 5 THEN '믹서기'
                                 WHEN 6 THEN '전기포트'
                                 WHEN 7 THEN '토스터'
                                 WHEN 8 THEN '오븐'
                                 WHEN 9 THEN '전자레인지'
                                 WHEN 10 THEN '커피머신'
                                 WHEN 11 THEN '착즙기'
                                 WHEN 12 THEN '제빵기'
                                 WHEN 13 THEN '전기그릴'
                                 ELSE '주방가전'
                                 END || ' ' || ((num - 1) / 34)::TEXT || '호'
                 -- 코웨이 - HOME_APPLIANCE
                 WHEN 5 THEN '코웨이 ' ||
                             CASE ((num - 1) / 34) % 15
                                 WHEN 0 THEN '아이콘 공기청정기'
                                 WHEN 1 THEN '에어메가'
                                 WHEN 2 THEN '정수기'
                                 WHEN 3 THEN '비데'
                                 WHEN 4 THEN '매트리스'
                                 WHEN 5 THEN '연수기'
                                 WHEN 6 THEN '가습기'
                                 WHEN 7 THEN '제습기'
                                 WHEN 8 THEN '무선청소기'
                                 WHEN 9 THEN '로봇청소기'
                                 WHEN 10 THEN '침구청소기'
                                 WHEN 11 THEN '스타일러'
                                 WHEN 12 THEN '얼음정수기'
                                 WHEN 13 THEN '온수매트'
                                 ELSE '생활가전'
                                 END || ' ' || ((num - 1) / 34)::TEXT || '호'
                 -- 르노코리아 - AUTO_PARTS
                 WHEN 6 THEN '르노 순정 ' ||
                             CASE ((num - 1) / 34) % 20
                                 WHEN 0 THEN '타이어'
                                 WHEN 1 THEN '배터리'
                                 WHEN 2 THEN '브레이크 패드'
                                 WHEN 3 THEN '브레이크 디스크'
                                 WHEN 4 THEN '에어필터'
                                 WHEN 5 THEN '오일필터'
                                 WHEN 6 THEN '연료필터'
                                 WHEN 7 THEN '와이퍼'
                                 WHEN 8 THEN '엔진오일'
                                 WHEN 9 THEN '미션오일'
                                 WHEN 10 THEN '냉각수'
                                 WHEN 11 THEN '점화플러그'
                                 WHEN 12 THEN '헤드라이트'
                                 WHEN 13 THEN '안개등'
                                 WHEN 14 THEN '사이드미러'
                                 WHEN 15 THEN '도어핸들'
                                 WHEN 16 THEN '범퍼'
                                 WHEN 17 THEN '휠커버'
                                 WHEN 18 THEN '시트커버'
                                 ELSE '부품'
                                 END || ' ' || ((num - 1) / 34)::TEXT || '호'
                 -- 동원F&B - FOOD
                 WHEN 7 THEN '동원 ' ||
                             CASE ((num - 1) / 34) % 20
                                 WHEN 0 THEN '참치 100g'
                                 WHEN 1 THEN '참치 150g'
                                 WHEN 2 THEN '양반김'
                                 WHEN 3 THEN '리챔'
                                 WHEN 4 THEN '소시지'
                                 WHEN 5 THEN '비엔나'
                                 WHEN 6 THEN '양반김치'
                                 WHEN 7 THEN '덴마크'
                                 WHEN 8 THEN '고등어구이'
                                 WHEN 9 THEN '꽁치구이'
                                 WHEN 10 THEN '연어훈제'
                                 WHEN 11 THEN '오징어채'
                                 WHEN 12 THEN '진미채'
                                 WHEN 13 THEN '육포'
                                 WHEN 14 THEN '참치죽'
                                 WHEN 15 THEN '전복죽'
                                 WHEN 16 THEN '단호박죽'
                                 WHEN 17 THEN '흑임자죽'
                                 WHEN 18 THEN '닭가슴살'
                                 ELSE '식품'
                                 END || ' ' || ((num - 1) / 34)::TEXT || '호'
                 -- 삼성SDI - AUTO_PARTS
                 WHEN 8 THEN
                     CASE ((num - 1) / 34) % 15
                         WHEN 0 THEN '전기차 배터리팩 60kWh'
                         WHEN 1 THEN '전기차 배터리팩 75kWh'
                         WHEN 2 THEN '전기차 배터리팩 100kWh'
                         WHEN 3 THEN 'ESS 배터리 시스템'
                         WHEN 4 THEN '노트북 배터리'
                         WHEN 5 THEN '스마트폰 배터리'
                         WHEN 6 THEN '보조배터리'
                         WHEN 7 THEN '하이브리드 배터리'
                         WHEN 8 THEN 'PHEV 배터리'
                         WHEN 9 THEN '전동킥보드 배터리'
                         WHEN 10 THEN '전기자전거 배터리'
                         WHEN 11 THEN '드론 배터리'
                         WHEN 12 THEN '전동공구 배터리'
                         WHEN 13 THEN '캠핑용 배터리'
                         ELSE '배터리'
                         END || ' ' || ((num - 1) / 34)::TEXT || '호'
                 -- 코오롱인더스트리 - CLOTHING
                 WHEN 9 THEN '코오롱 ' ||
                             CASE ((num - 1) / 34) % 15
                                 WHEN 0 THEN '등산복 상의'
                                 WHEN 1 THEN '등산복 하의'
                                 WHEN 2 THEN '등산화'
                                 WHEN 3 THEN '패딩'
                                 WHEN 4 THEN '윈드브레이커'
                                 WHEN 5 THEN '티셔츠'
                                 WHEN 6 THEN '후드티'
                                 WHEN 7 THEN '플리스'
                                 WHEN 8 THEN '레깅스'
                                 WHEN 9 THEN '배낭'
                                 WHEN 10 THEN '모자'
                                 WHEN 11 THEN '장갑'
                                 WHEN 12 THEN '양말'
                                 WHEN 13 THEN '넥워머'
                                 ELSE '의류'
                                 END || ' ' || ((num - 1) / 34)::TEXT || '호'
                 -- SK하이닉스 - COMPUTER_PARTS
                 WHEN 10 THEN
                     CASE ((num - 1) / 34) % 10
                         WHEN 0 THEN 'DDR5 메모리 32GB'
                         WHEN 1 THEN 'DDR5 메모리 16GB'
                         WHEN 2 THEN 'DDR4 메모리 16GB'
                         WHEN 3 THEN 'DDR4 메모리 8GB'
                         WHEN 4 THEN 'SSD 1TB'
                         WHEN 5 THEN 'SSD 512GB'
                         WHEN 6 THEN 'SSD 256GB'
                         WHEN 7 THEN 'M.2 SSD 2TB'
                         WHEN 8 THEN 'M.2 SSD 1TB'
                         ELSE '메모리/SSD'
                         END || ' ' || ((num - 1) / 34)::TEXT || '호'
                 -- 한진 - INDUSTRIAL
                 WHEN 11 THEN '한진 ' ||
                              CASE ((num - 1) / 34) % 10
                                  WHEN 0 THEN '파레트'
                                  WHEN 1 THEN '컨테이너'
                                  WHEN 2 THEN '지게차'
                                  WHEN 3 THEN '운반대차'
                                  WHEN 4 THEN '랙'
                                  WHEN 5 THEN '포장재'
                                  WHEN 6 THEN '테이프'
                                  WHEN 7 THEN '스트레치필름'
                                  WHEN 8 THEN '완충재'
                                  ELSE '물류용품'
                                  END || ' ' || ((num - 1) / 34)::TEXT || '호'
                 -- 기아자동차 - AUTO_PARTS
                 WHEN 12 THEN '기아 순정 ' ||
                              CASE ((num - 1) / 34) % 15
                                  WHEN 0 THEN '타이어'
                                  WHEN 1 THEN '배터리'
                                  WHEN 2 THEN '엔진오일'
                                  WHEN 3 THEN '브레이크패드'
                                  WHEN 4 THEN '에어필터'
                                  WHEN 5 THEN '오일필터'
                                  WHEN 6 THEN '와이퍼'
                                  WHEN 7 THEN '헤드라이트'
                                  WHEN 8 THEN '범퍼'
                                  WHEN 9 THEN '사이드미러'
                                  WHEN 10 THEN '도어핸들'
                                  WHEN 11 THEN '후드'
                                  WHEN 12 THEN '트렁크'
                                  WHEN 13 THEN '휠'
                                  ELSE '부품'
                                  END || ' ' || ((num - 1) / 34)::TEXT || '호'
                 -- 금호타이어 - AUTO_PARTS
                 WHEN 13 THEN '금호 ' ||
                              CASE ((num - 1) / 34) % 10
                                  WHEN 0 THEN '사계절타이어 235/60R18'
                                  WHEN 1 THEN '사계절타이어 225/55R17'
                                  WHEN 2 THEN '사계절타이어 215/50R17'
                                  WHEN 3 THEN '스노우타이어 235/60R18'
                                  WHEN 4 THEN '스노우타이어 225/55R17'
                                  WHEN 5 THEN '서머타이어 245/45R19'
                                  WHEN 6 THEN '서머타이어 235/45R18'
                                  WHEN 7 THEN 'SUV타이어 265/65R17'
                                  WHEN 8 THEN 'SUV타이어 255/60R18'
                                  ELSE '타이어'
                                  END || ' ' || ((num - 1) / 34)::TEXT || '호'
                 -- 한화에어로스페이스 - INDUSTRIAL
                 WHEN 14 THEN '한화 ' ||
                              CASE ((num - 1) / 34) % 10
                                  WHEN 0 THEN '항공기 부품'
                                  WHEN 1 THEN '엔진 부품'
                                  WHEN 2 THEN '방산 부품'
                                  WHEN 3 THEN '우주 부품'
                                  WHEN 4 THEN '산업용 터빈'
                                  WHEN 5 THEN '발전기'
                                  WHEN 6 THEN '압축기'
                                  WHEN 7 THEN '펌프'
                                  WHEN 8 THEN '밸브'
                                  ELSE '산업재'
                                  END || ' ' || ((num - 1) / 34)::TEXT || '호'
                 -- 로지텍코리아 - COMPUTER_PERIPHERAL
                 WHEN 15 THEN '로지텍 ' ||
                              CASE ((num - 1) / 34) % 15
                                  WHEN 0 THEN 'MX Master 마우스'
                                  WHEN 1 THEN 'MX Keys 키보드'
                                  WHEN 2 THEN 'MX Anywhere 마우스'
                                  WHEN 3 THEN 'MX Keys Mini'
                                  WHEN 4 THEN 'G502 게이밍 마우스'
                                  WHEN 5 THEN 'G913 게이밍 키보드'
                                  WHEN 6 THEN 'G PRO 헤드셋'
                                  WHEN 7 THEN 'C920 웹캠'
                                  WHEN 8 THEN 'C930e 웹캠'
                                  WHEN 9 THEN 'Z407 스피커'
                                  WHEN 10 THEN 'Z625 스피커'
                                  WHEN 11 THEN 'Spotlight 프레젠터'
                                  WHEN 12 THEN '무선 마우스 M650'
                                  WHEN 13 THEN '무선 키보드 K380'
                                  ELSE '주변기기'
                                  END || ' ' || ((num - 1) / 34)::TEXT || '호'
                 -- 현대자동차 - AUTO_PARTS
                 WHEN 16 THEN '현대 순정 ' ||
                              CASE ((num - 1) / 34) % 15
                                  WHEN 0 THEN '에어필터'
                                  WHEN 1 THEN '오일필터'
                                  WHEN 2 THEN '연료필터'
                                  WHEN 3 THEN '브레이크패드 앞'
                                  WHEN 4 THEN '브레이크패드 뒤'
                                  WHEN 5 THEN '브레이크디스크'
                                  WHEN 6 THEN '와이퍼 블레이드'
                                  WHEN 7 THEN '엔진오일 5W30'
                                  WHEN 8 THEN '엔진오일 5W40'
                                  WHEN 9 THEN '배터리 75Ah'
                                  WHEN 10 THEN '타이어 235/60R18'
                                  WHEN 11 THEN '헤드라이트 전구'
                                  WHEN 12 THEN '안개등 전구'
                                  WHEN 13 THEN '점화플러그'
                                  ELSE '부품'
                                  END || ' ' || ((num - 1) / 34)::TEXT || '호'
                 -- SK에너지 - INDUSTRIAL
                 WHEN 17 THEN 'SK ' ||
                              CASE ((num - 1) / 34) % 10
                                  WHEN 0 THEN '휘발유 첨가제'
                                  WHEN 1 THEN '경유 첨가제'
                                  WHEN 2 THEN '윤활유'
                                  WHEN 3 THEN '그리스'
                                  WHEN 4 THEN '산업용 오일'
                                  WHEN 5 THEN '절삭유'
                                  WHEN 6 THEN '유압유'
                                  WHEN 7 THEN '기어오일'
                                  WHEN 8 THEN '방청유'
                                  ELSE '화학제품'
                                  END || ' ' || ((num - 1) / 34)::TEXT || '호'
                 -- 오뚜기 - FOOD
                 WHEN 18 THEN '오뚜기 ' ||
                              CASE ((num - 1) / 34) % 20
                                  WHEN 0 THEN '진라면 매운맛'
                                  WHEN 1 THEN '진라면 순한맛'
                                  WHEN 2 THEN '진짬뽕'
                                  WHEN 3 THEN '카레 중辛'
                                  WHEN 4 THEN '카레 순한맛'
                                  WHEN 5 THEN '케찹'
                                  WHEN 6 THEN '마요네즈'
                                  WHEN 7 THEN '참기름'
                                  WHEN 8 THEN '들기름'
                                  WHEN 9 THEN '올리브유'
                                  WHEN 10 THEN '카놀라유'
                                  WHEN 11 THEN '미원'
                                  WHEN 12 THEN '스파게티면'
                                  WHEN 13 THEN '토마토소스'
                                  WHEN 14 THEN '짜장소스'
                                  WHEN 15 THEN '돈가스소스'
                                  WHEN 16 THEN '우스터소스'
                                  WHEN 17 THEN '참치마요'
                                  WHEN 18 THEN '옛날 누룽지'
                                  ELSE '식품'
                                  END || ' ' || ((num - 1) / 34)::TEXT || '호'
                 -- 풀무원 - FOOD
                 WHEN 19 THEN '풀무원 ' ||
                              CASE ((num - 1) / 34) % 15
                                  WHEN 0 THEN '두부 300g'
                                  WHEN 1 THEN '두부 500g'
                                  WHEN 2 THEN '순두부'
                                  WHEN 3 THEN '연두부'
                                  WHEN 4 THEN '생면 우동'
                                  WHEN 5 THEN '생면 중화면'
                                  WHEN 6 THEN '생면 라면'
                                  WHEN 7 THEN '김치'
                                  WHEN 8 THEN '샐러드'
                                  WHEN 9 THEN '샌드위치'
                                  WHEN 10 THEN '유부초밥'
                                  WHEN 11 THEN '떡볶이'
                                  WHEN 12 THEN '만두'
                                  WHEN 13 THEN '어묵'
                                  ELSE '식품'
                                  END || ' ' || ((num - 1) / 34)::TEXT || '호'
                 -- 하이트진로 - BEVERAGE
                 WHEN 20 THEN
                     CASE ((num - 1) / 34) % 10
                         WHEN 0 THEN '참이슬 오리지널'
                         WHEN 1 THEN '참이슬 후레쉬'
                         WHEN 2 THEN '진로이즈백'
                         WHEN 3 THEN '테라'
                         WHEN 4 THEN '하이트'
                         WHEN 5 THEN '필라이트'
                         WHEN 6 THEN '엑스트라콜드'
                         WHEN 7 THEN '맥스'
                         WHEN 8 THEN '켈리'
                         ELSE '주류'
                         END || ' ' || ((num - 1) / 34)::TEXT || '호'
                 -- 농심 - FOOD
                 WHEN 21 THEN '농심 ' ||
                              CASE ((num - 1) / 34) % 20
                                  WHEN 0 THEN '신라면'
                                  WHEN 1 THEN '신라면 블랙'
                                  WHEN 2 THEN '짜파게티'
                                  WHEN 3 THEN '너구리'
                                  WHEN 4 THEN '안성탕면'
                                  WHEN 5 THEN '새우깡'
                                  WHEN 6 THEN '양파깡'
                                  WHEN 7 THEN '포테토칩'
                                  WHEN 8 THEN '수미칩'
                                  WHEN 9 THEN '바나나킥'
                                  WHEN 10 THEN '오징어집'
                                  WHEN 11 THEN '꿀꽈배기'
                                  WHEN 12 THEN '조청유과'
                                  WHEN 13 THEN '쌀새우깡'
                                  WHEN 14 THEN '감자깡'
                                  WHEN 15 THEN '매운새우깡'
                                  WHEN 16 THEN '올리브짭짤이'
                                  WHEN 17 THEN '자갈치'
                                  WHEN 18 THEN '스윙칩'
                                  ELSE '식품'
                                  END || ' ' || ((num - 1) / 34)::TEXT || '호'
                 -- SK바이오팜 - PHARMACEUTICAL
                 WHEN 22 THEN 'SK바이오팜 ' ||
                              CASE ((num - 1) / 34) % 10
                                  WHEN 0 THEN '세노바메이트'
                                  WHEN 1 THEN '항전간제'
                                  WHEN 2 THEN '신경치료제'
                                  WHEN 3 THEN '진통제'
                                  WHEN 4 THEN '소염제'
                                  WHEN 5 THEN '항생제'
                                  WHEN 6 THEN '비타민'
                                  WHEN 7 THEN '영양제'
                                  WHEN 8 THEN '건강기능식품'
                                  ELSE '의약품'
                                  END || ' ' || ((num - 1) / 34)::TEXT || '호'
                 -- 한국타이어 - AUTO_PARTS
                 WHEN 23 THEN '한국타이어 ' ||
                              CASE ((num - 1) / 34) % 10
                                  WHEN 0 THEN '벤투스 S1 245/45R19'
                                  WHEN 1 THEN '벤투스 V12 235/40R18'
                                  WHEN 2 THEN '키너지 EX 235/60R18'
                                  WHEN 3 THEN '키너지 GT 225/55R17'
                                  WHEN 4 THEN '윈터 아이셉트 235/60R18'
                                  WHEN 5 THEN '다이나프로 HP2 235/60R18'
                                  WHEN 6 THEN '다이나프로 AT2 265/70R17'
                                  WHEN 7 THEN 'K125 175/70R14'
                                  WHEN 8 THEN 'K115 185/65R15'
                                  ELSE '타이어'
                                  END || ' ' || ((num - 1) / 34)::TEXT || '호'
                 -- 삼성디스플레이 - ELECTRONIC_PARTS
                 WHEN 24 THEN '삼성디스플레이 ' ||
                              CASE ((num - 1) / 34) % 10
                                  WHEN 0 THEN 'OLED 패널 55인치'
                                  WHEN 1 THEN 'OLED 패널 65인치'
                                  WHEN 2 THEN 'OLED 패널 77인치'
                                  WHEN 3 THEN 'LCD 패널 32인치'
                                  WHEN 4 THEN 'LCD 패널 43인치'
                                  WHEN 5 THEN 'QLED 패널'
                                  WHEN 6 THEN '플렉서블 OLED'
                                  WHEN 7 THEN '투명 OLED'
                                  WHEN 8 THEN '마이크로 LED'
                                  ELSE '디스플레이'
                                  END || ' ' || ((num - 1) / 34)::TEXT || '호'
                 -- 현대제철 - INDUSTRIAL
                 WHEN 25 THEN '현대제철 ' ||
                              CASE ((num - 1) / 34) % 10
                                  WHEN 0 THEN '열연강판'
                                  WHEN 1 THEN '냉연강판'
                                  WHEN 2 THEN '아연도강판'
                                  WHEN 3 THEN '전기강판'
                                  WHEN 4 THEN 'H형강'
                                  WHEN 5 THEN 'I형강'
                                  WHEN 6 THEN '철근'
                                  WHEN 7 THEN '선재'
                                  WHEN 8 THEN '봉강'
                                  ELSE '철강재'
                                  END || ' ' || ((num - 1) / 34)::TEXT || '호'
                 -- 현대모비스 - AUTO_PARTS
                 WHEN 26 THEN '현대모비스 ' ||
                              CASE ((num - 1) / 34) % 15
                                  WHEN 0 THEN '에어백'
                                  WHEN 1 THEN 'ABS 모듈'
                                  WHEN 2 THEN 'ECU'
                                  WHEN 3 THEN '파워스티어링'
                                  WHEN 4 THEN '서스펜션'
                                  WHEN 5 THEN '샤시 부품'
                                  WHEN 6 THEN '등화장치'
                                  WHEN 7 THEN '와이퍼 시스템'
                                  WHEN 8 THEN '미러'
                                  WHEN 9 THEN '범퍼'
                                  WHEN 10 THEN '라디에이터'
                                  WHEN 11 THEN '배터리 관리 시스템'
                                  WHEN 12 THEN '연료탱크'
                                  WHEN 13 THEN '배기 시스템'
                                  ELSE '자동차 부품'
                                  END || ' ' || ((num - 1) / 34)::TEXT || '호'
                 -- 삼양식품 - FOOD
                 WHEN 27 THEN '삼양 ' ||
                              CASE ((num - 1) / 34) % 15
                                  WHEN 0 THEN '불닭볶음면'
                                  WHEN 1 THEN '불닭볶음면 까르보나라'
                                  WHEN 2 THEN '불닭볶음면 치즈'
                                  WHEN 3 THEN '불닭볶음면 핵불닭'
                                  WHEN 4 THEN '삼양라면'
                                  WHEN 5 THEN '삼양라면 큰사발'
                                  WHEN 6 THEN '나가사끼짬뽕'
                                  WHEN 7 THEN '꼬꼬면'
                                  WHEN 8 THEN '열라면'
                                  WHEN 9 THEN '짜장불닭'
                                  WHEN 10 THEN '로제불닭'
                                  WHEN 11 THEN '김치라면'
                                  WHEN 12 THEN '큰사발 불닭볶음면'
                                  WHEN 13 THEN '팔도비빔면'
                                  ELSE '라면'
                                  END || ' ' || ((num - 1) / 34)::TEXT || '호'
                 -- 포스코케미칼 - INDUSTRIAL
                 WHEN 28 THEN '포스코케미칼 ' ||
                              CASE ((num - 1) / 34) % 10
                                  WHEN 0 THEN '양극재'
                                  WHEN 1 THEN '음극재'
                                  WHEN 2 THEN '전해액'
                                  WHEN 3 THEN '분리막'
                                  WHEN 4 THEN '수산화리튬'
                                  WHEN 5 THEN '탄산리튬'
                                  WHEN 6 THEN '니켈'
                                  WHEN 7 THEN '코발트'
                                  WHEN 8 THEN '망간'
                                  ELSE '2차전지 소재'
                                  END || ' ' || ((num - 1) / 34)::TEXT || '호'
                 -- GS칼텍스 - INDUSTRIAL
                 WHEN 29 THEN 'GS칼텍스 ' ||
                              CASE ((num - 1) / 34) % 10
                                  WHEN 0 THEN '휘발유 첨가제'
                                  WHEN 1 THEN '경유 첨가제'
                                  WHEN 2 THEN '엔진오일'
                                  WHEN 3 THEN '기어오일'
                                  WHEN 4 THEN '브레이크오일'
                                  WHEN 5 THEN '부동액'
                                  WHEN 6 THEN '워셔액'
                                  WHEN 7 THEN '산업용 윤활유'
                                  WHEN 8 THEN '그리스'
                                  ELSE '석유제품'
                                  END || ' ' || ((num - 1) / 34)::TEXT || '호'
                 -- 포스코 - INDUSTRIAL
                 WHEN 30 THEN '포스코 ' ||
                              CASE ((num - 1) / 34) % 10
                                  WHEN 0 THEN '열연강판'
                                  WHEN 1 THEN '냉연강판'
                                  WHEN 2 THEN '후판'
                                  WHEN 3 THEN '선재'
                                  WHEN 4 THEN '봉강'
                                  WHEN 5 THEN '강관'
                                  WHEN 6 THEN '형강'
                                  WHEN 7 THEN '스테인리스'
                                  WHEN 8 THEN '전기강판'
                                  ELSE '철강제품'
                                  END || ' ' || ((num - 1) / 34)::TEXT || '호'
                 -- 삼성전기 - ELECTRONIC_PARTS
                 WHEN 31 THEN '삼성전기 ' ||
                              CASE ((num - 1) / 34) % 10
                                  WHEN 0 THEN 'MLCC 적층세라믹콘덴서'
                                  WHEN 1 THEN '카메라모듈'
                                  WHEN 2 THEN '칩인덕터'
                                  WHEN 3 THEN '칩저항'
                                  WHEN 4 THEN '칩페라이트비드'
                                  WHEN 5 THEN 'PKG기판'
                                  WHEN 6 THEN 'FC-BGA'
                                  WHEN 7 THEN '통신모듈'
                                  WHEN 8 THEN '전력모듈'
                                  ELSE '전자부품'
                                  END || ' ' || ((num - 1) / 34)::TEXT || '호'
                 -- 한국항공우주 - INDUSTRIAL
                 WHEN 32 THEN '한국항공우주 ' ||
                              CASE ((num - 1) / 34) % 10
                                  WHEN 0 THEN '항공기 동체'
                                  WHEN 1 THEN '항공기 날개'
                                  WHEN 2 THEN '엔진 부품'
                                  WHEN 3 THEN '랜딩기어'
                                  WHEN 4 THEN '항전장비'
                                  WHEN 5 THEN '위성체'
                                  WHEN 6 THEN '로켓 부품'
                                  WHEN 7 THEN '드론 부품'
                                  WHEN 8 THEN '헬기 부품'
                                  ELSE '항공우주 부품'
                                  END || ' ' || ((num - 1) / 34)::TEXT || '호'
                 -- 두산중공업 - INDUSTRIAL
                 WHEN 33 THEN '두산중공업 ' ||
                              CASE ((num - 1) / 34) % 10
                                  WHEN 0 THEN '가스터빈'
                                  WHEN 1 THEN '증기터빈'
                                  WHEN 2 THEN '발전기'
                                  WHEN 3 THEN '보일러'
                                  WHEN 4 THEN '열교환기'
                                  WHEN 5 THEN '압축기'
                                  WHEN 6 THEN '펌프'
                                  WHEN 7 THEN '해수담수화설비'
                                  WHEN 8 THEN '원전기기'
                                  ELSE '발전설비'
                                  END || ' ' || ((num - 1) / 34)::TEXT || '호'
                 END AS product_name,
             -- 카테고리 할당
             CASE
                 WHEN (num - 1) % 34 IN (0, 1, 2) THEN 'ELECTRONICS'
                 WHEN (num - 1) % 34 = 3 THEN 'FURNITURE'
                 WHEN (num - 1) % 34 = 4 THEN 'KITCHEN_APPLIANCE'
                 WHEN (num - 1) % 34 = 5 THEN 'HOME_APPLIANCE'
                 WHEN (num - 1) % 34 IN (6, 8, 12, 13, 16, 23, 26) THEN 'AUTO_PARTS'
                 WHEN (num - 1) % 34 IN (7, 18, 19, 21, 27) THEN 'FOOD'
                 WHEN (num - 1) % 34 = 9 THEN 'CLOTHING'
                 WHEN (num - 1) % 34 = 10 THEN 'COMPUTER_PARTS'
                 WHEN (num - 1) % 34 IN (11, 14, 17, 25, 28, 29, 30, 32, 33) THEN 'INDUSTRIAL'
                 WHEN (num - 1) % 34 = 15 THEN 'COMPUTER_PERIPHERAL'
                 WHEN (num - 1) % 34 = 20 THEN 'BEVERAGE'
                 WHEN (num - 1) % 34 = 22 THEN 'PHARMACEUTICAL'
                 WHEN (num - 1) % 34 IN (24, 31) THEN 'ELECTRONIC_PARTS'
                 ELSE 'ETC'
                 END AS category
      FROM generate_series(1, 10000) AS num) AS products_data
ON CONFLICT (product_id) DO NOTHING;

-- 재고 데이터 (10,000개 상품, 각 상품은 자신의 업체가 속한 허브에만 재고 보유)
INSERT INTO p_inventory (inventory_id, product_id, hub_id, quantity, created_at, updated_at)
SELECT ('dddddddd-0000-0000-0000-' || LPAD(num::TEXT, 12, '0'))::UUID                        AS inventory_id,
       ('bbbbbbbb-0000-0000-0000-' || LPAD(num::TEXT, 12, '0'))::UUID                        AS product_id,
       -- 상품의 업체가 속한 허브 ID 계산
       ('aaaaaaaa-0000-0000-0002-' || LPAD((((num - 1) % 34) / 2 + 1)::TEXT, 12, '0'))::UUID AS hub_id,
       10000000                                                                              AS quantity, -- 1만개
       NOW()                                                                                 AS created_at,
       NOW()                                                                                 AS updated_at
FROM generate_series(1, 10000) AS num
ON CONFLICT (product_id, hub_id) DO NOTHING;