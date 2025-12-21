# Hot Product 부하 테스트

Redis 기반 Hot Product 재고 선점 시스템의 부하 테스트 스크립트입니다.

## 사전 조건

1. Hub 서비스 실행 중 (기본 포트: 8030)
2. Redis 실행 중
3. DB에 테스트 상품 데이터 존재 (data.sql)

## 테스트 스크립트

### 1. register-setup.js

Hot Product 등록 (테스트 전 1회 실행)

```bash
k6 run --out influxdb=http://localhost:8086/k6 register-setup.js
```

### 2. reserve-smoke.js

기본 동작 확인 (5 VUs, 10 iterations)

```bash
k6 run --out influxdb=http://localhost:8086/k6 reserve-smoke.js
```

### 3. reserve-load.js

Hot Product 부하 테스트 (30 VUs, 30초) - Redis 기반

```bash
k6 run --out influxdb=http://localhost:8086/k6 reserve-load.js
```

### 4. reserve-normal-load.js

일반 상품 부하 테스트 (30 VUs, 30초) - DB 기반

```bash
k6 run --out influxdb=http://localhost:8086/k6 reserve-normal-load.js
```

### 5. reserve-spike.js

스파이크 테스트 (0 → 500 VUs)

```bash
k6 run --out influxdb=http://localhost:8086/k6 reserve-spike.js
```

## 환경변수

| 변수               | 설명          | 기본값                                  |
|------------------|-------------|--------------------------------------|
| HUB_BASE_URL     | Hub 서비스 URL | http://localhost:8030                |
| PRODUCT_ID       | 테스트 상품 ID   | bbbbbbbb-0000-0000-0000-000000000001 |
| HUB_ID           | 테스트 허브 ID   | aaaaaaaa-0000-0000-0002-000000000001 |
| TTL_SECONDS      | 캐시 TTL (초)  | 3600                                 |
| RESERVE_QUANTITY | 선점 수량       | 1                                    |

## 성능 비교 테스트 방법

### Hot Product vs 일반 상품 비교

1. **일반 상품 테스트 (DB 기반)**

```bash
# 상품 2번은 Hot Product로 등록하지 않음
k6 run --out influxdb=http://localhost:8086/k6 load-test/hot-product/reserve-normal-load.js
```

2. **Hot Product 테스트 (Redis 기반)**

```bash
# 먼저 Hot Product 등록
k6 run --out influxdb=http://localhost:8086/k6 load-test/hot-product/register-setup.js

# 부하 테스트 실행
k6 run --out influxdb=http://localhost:8086/k6 load-test/hot-product/reserve-load.js
```

3. **결과 비교**

- 응답 시간 (p95, p99)
- 처리량 (requests/sec)
- 에러율

## 테스트 데이터

| 상품           | productId                            | hubId                                | 용도              |
|--------------|--------------------------------------|--------------------------------------|-----------------|
| 상품 1 (삼성전자)  | bbbbbbbb-0000-0000-0000-000000000001 | aaaaaaaa-0000-0000-0002-000000000001 | Hot Product 테스트 |
| 상품 2 (애플코리아) | bbbbbbbb-0000-0000-0000-000000000002 | aaaaaaaa-0000-0000-0002-000000000001 | 일반 상품 테스트       |