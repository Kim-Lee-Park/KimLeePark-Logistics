# 주문 생성 부하 시나리오 (단계별 개별 실행)
- 대상: `POST /v1/orders` (Gateway 경유 시 `http://<gateway>:8080/v1/orders`, 직접 호출 시 주문 서비스 포트)
- 필수 선행
  - DB 시드: `hub/src/main/resources/data.sql`, `load-test/seed_master_user.sql`
  - 재고 시드(선택): `load-test/order/seed_inventory_for_order_test.sql` (테스트 전 재고를 원하는 수량으로 초기화)
  - 필수 ID: `SUPPLIER_ID`, `PRODUCT_ID`, `HUB_ID`, `USER_ID`(생성자), `ADDRESS_ID`(UUID 아무 값)
  - Redis/Kafka 동작 필수
- 실행 환경변수 공통
  - `TARGET_BASE_URL` (주문 API 베이스)
  - `HUB_BASE_URL` (허브 API 베이스, 재고 조회용)
  - `SUPPLIER_ID`, `PRODUCT_ID`, `HUB_ID`, `USER_ID`, `ADDRESS_ID`
  - `EXPECTED_ORDERS` (각 스크립트의 예상 주문 건수; 재고를 동일 수량으로 맞추면 테어다운에서 재고 0 여부를 확인)

## 시나리오별 k6 스크립트 (각각 단독 실행)
- `create-order-smoke.js`: shared-iterations 10건, VU 5 → 기본 동작 확인
- `create-order-ramp.js`: 2→10 rps(1m), 10→30 rps(2m) → 낮은 TPS 램프업
- `create-order-soak.js`: 10 rps, 3m → 가벼운 장기 실행
- `create-order-step.js`: 10→30 rps(2m) 후 30→0(30s) → 급상승 구간 체크

## 실행 예시 (Smoke)
```bash
export TARGET_BASE_URL=http://localhost:18080
export HUB_BASE_URL=http://localhost:18030
export SUPPLIER_ID=<uuid>
export PRODUCT_ID=<uuid>
export HUB_ID=<uuid>
export USER_ID=1
export ADDRESS_ID=<uuid>
export EXPECTED_ORDERS=10  # 초기 재고를 동일 수량으로 세팅해두면 테어다운에서 재고 0을 확인

k6 run load-test/order/create-order-smoke.js
```
