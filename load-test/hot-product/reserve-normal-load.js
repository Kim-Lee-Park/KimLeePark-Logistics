/**
 * 일반 상품 (Non-Hot Product) 재고 선점 부하 테스트
 *
 * Redis 캐시 없이 DB 직접 처리 방식
 * Hot Product와 성능 비교용
 *
 * 실행:
 * k6 run load-test/hot-product/reserve-normal-load.js
 *
 * 환경변수:
 * - HUB_BASE_URL: Hub 서비스 URL (기본: http://localhost:8030)
 * - PRODUCT_ID: 테스트 상품 ID (Hot Product로 등록되지 않은 상품)
 * - HUB_ID: 테스트 허브 ID
 * - RESERVE_QUANTITY: 선점 수량 (기본: 1)
 */
import http from 'k6/http';
import {check, sleep} from 'k6';
import {uuidv4} from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';
import {Counter, Trend} from 'k6/metrics';

const successCounter = new Counter('successful_reservations');
const failedCounter = new Counter('failed_reservations');
const reserveDuration = new Trend('reserve_duration');

// 일반 상품 (Hot Product로 등록되지 않은 상품)
const cfg = {
  hubBaseUrl: __ENV.HUB_BASE_URL || 'http://localhost:8030',
  // 상품 2번 (LG전자) - Hot Product로 등록하지 않음
  productId: __ENV.PRODUCT_ID || 'bbbbbbbb-0000-0000-0000-000000000002',
  hubId: __ENV.HUB_ID || 'aaaaaaaa-0000-0000-0002-000000000001',
  reserveQuantity: __ENV.RESERVE_QUANTITY || '1',
};

const headers = {
  'Content-Type': 'application/json',
};

export const options = {
  scenarios: {
    load: {
      executor: 'constant-vus',
      vus: 30,
      duration: '30s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.1'],
    http_req_duration: ['p(95)<2000', 'p(99)<3000'],
  },
};

function reserveInventory() {
  const orderId = uuidv4();
  const idempotencyKey = `normal-load-test-${orderId}`;

  const payload = {
    orderId: orderId,
    idempotencyKey: idempotencyKey,
    items: [
      {
        productId: cfg.productId,
        hubId: cfg.hubId,
        quantity: Number(cfg.reserveQuantity),
      },
    ],
  };

  const startTime = Date.now();
  const res = http.post(
    `${cfg.hubBaseUrl}/internal/v1/inventories/reserve`,
    JSON.stringify(payload),
    {headers}
  );
  const duration = Date.now() - startTime;
  reserveDuration.add(duration);

  const success = check(res, {
    'reserve status 200': (r) => r.status === 200,
    'reserve not failed': (r) => r.status !== 400,
  });

  sleep(0.01);
  return {res, orderId, success};
}

export function setup() {
  console.log(`=== 일반 상품 (Non-Hot Product) 부하 테스트 ===`);
  console.log(`상품 ID: ${cfg.productId}`);
  console.log(`허브 ID: ${cfg.hubId}`);
  console.log(`선점 수량: ${cfg.reserveQuantity}`);
  console.log(`이 상품은 Redis 캐시 없이 DB에서 직접 처리됩니다.`);
  return {};
}

export default function () {
  const result = reserveInventory();

  if (result.success) {
    successCounter.add(1);
  } else {
    failedCounter.add(1);
  }
}

export function teardown(data) {
  console.log(`=== 테스트 완료 ===`);
  console.log(`일반 상품 (DB 직접 처리) 테스트 종료`);
}
