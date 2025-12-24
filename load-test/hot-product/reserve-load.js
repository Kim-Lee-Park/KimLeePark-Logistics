/**
 * Hot Product 재고 선점 부하 테스트
 *
 * 동시 100명 사용자가 재고 선점 요청
 * Redis Lua Script의 원자성 검증
 *
 * 실행 전: Hot Product 등록 필요
 * k6 run load-test/hot-product/register-setup.js
 *
 * 실행:
 * k6 run load-test/hot-product/reserve-load.js
 *
 * 환경변수:
 * - HUB_BASE_URL: Hub 서비스 URL (기본: http://localhost:8030)
 * - PRODUCT_ID: 테스트 상품 ID
 * - HUB_ID: 테스트 허브 ID
 * - RESERVE_QUANTITY: 선점 수량 (기본: 1)
 */
import {getHotProductQuantity, reserveInventory} from './common.js';
import {Counter} from 'k6/metrics';

const successCounter = new Counter('successful_reservations');
const failedCounter = new Counter('failed_reservations');

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
    http_req_duration: ['p(95)<1000', 'p(99)<2000'],
  },
};

export function setup() {
  const res = getHotProductQuantity();
  console.log(`Initial quantity: ${res.body}`);
  return JSON.parse(res.body);
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
  const res = getHotProductQuantity();
  const finalData = JSON.parse(res.body);

  console.log(`=== 테스트 결과 ===`);
  console.log(`초기 재고: ${data.quantity}`);
  console.log(`최종 재고: ${finalData.quantity}`);
  console.log(`차감된 재고: ${data.quantity - finalData.quantity}`);
}
