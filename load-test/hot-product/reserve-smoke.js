/**
 * Hot Product 재고 선점 Smoke 테스트
 * 
 * 실행 전: Hot Product 등록 필요
 * k6 run load-test/hot-product/register-setup.js
 * 
 * 실행:
 * k6 run load-test/hot-product/reserve-smoke.js
 * 
 * 환경변수:
 * - HUB_BASE_URL: Hub 서비스 URL (기본: http://localhost:8030)
 * - PRODUCT_ID: 테스트 상품 ID
 * - HUB_ID: 테스트 허브 ID
 */
import {reserveInventory, getHotProductQuantity} from './common.js';

export const options = {
  scenarios: {
    smoke: {
      executor: 'shared-iterations',
      iterations: 10,
      vus: 5,
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<500'],
  },
};

export default function () {
  reserveInventory();
}

export function teardown() {
  const res = getHotProductQuantity();
  console.log(`Final quantity: ${res.body}`);
}
