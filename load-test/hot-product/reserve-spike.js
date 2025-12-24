/**
 * Hot Product 재고 선점 스파이크 테스트
 * 
 * 갑자기 500명이 동시에 요청하는 상황 시뮬레이션
 * 인기 상품 타임세일 시나리오
 * 
 * 실행 전: Hot Product 등록 필요 (재고 10000개)
 * k6 run load-test/hot-product/register-setup.js
 * 
 * 실행:
 * k6 run load-test/hot-product/reserve-spike.js
 */
import {reserveInventory, getHotProductQuantity} from './common.js';
import {Counter} from 'k6/metrics';

const successCounter = new Counter('successful_reservations');
const failedCounter = new Counter('failed_reservations');
const insufficientStock = new Counter('insufficient_stock');

export const options = {
  scenarios: {
    spike: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        {duration: '5s', target: 500},   // 5초 동안 500명까지 증가
        {duration: '10s', target: 500},  // 10초 동안 500명 유지
        {duration: '5s', target: 0},     // 5초 동안 0명으로 감소
      ],
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.2'],
    http_req_duration: ['p(95)<2000'],
  },
};

export function setup() {
  const res = getHotProductQuantity();
  console.log(`Initial quantity: ${res.body}`);
  return JSON.parse(res.body);
}

export default function () {
  const result = reserveInventory();
  
  if (result.res.status === 200) {
    successCounter.add(1);
  } else if (result.res.status === 400) {
    // 재고 부족
    insufficientStock.add(1);
  } else {
    failedCounter.add(1);
  }
}

export function teardown(data) {
  const res = getHotProductQuantity();
  const finalData = JSON.parse(res.body);
  
  console.log(`\n=== 스파이크 테스트 결과 ===`);
  console.log(`초기 재고: ${data.quantity}`);
  console.log(`최종 재고: ${finalData.quantity}`);
  console.log(`차감된 재고: ${data.quantity - finalData.quantity}`);
}
