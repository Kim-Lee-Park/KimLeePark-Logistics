import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';

// 환경 변수
const BASE_URL = __ENV.TARGET_BASE_URL || 'http://localhost:18080';
const SUPPLIER_ID = __ENV.SUPPLIER_ID;
const PRODUCT_ID = __ENV.PRODUCT_ID;
const HUB_ID = __ENV.HUB_ID;
const USER_ID = __ENV.USER_ID || '1';
const ADDRESS_ID = __ENV.ADDRESS_ID;

if (!SUPPLIER_ID || !PRODUCT_ID || !HUB_ID || !ADDRESS_ID) {
  throw new Error('SUPPLIER_ID, PRODUCT_ID, HUB_ID, ADDRESS_ID env가 필요합니다.');
}

const items = new SharedArray('order-items', () => [
  {
    productId: PRODUCT_ID,
    productName: 'load-test-item',
    hubId: HUB_ID,
    quantity: 1,
    price: 10000,
  },
]);

export const options = {
  discardResponseBodies: true,
  scenarios: {
    smoke: {
      executor: 'ramping-arrival-rate',
      startRate: 1,
      timeUnit: '1s',
      preAllocatedVUs: 10,
      maxVUs: 20,
      stages: [
        { target: 2, duration: '30s' },
      ],
    },
    ramp1: {
      executor: 'ramping-arrival-rate',
      startRate: 2,
      timeUnit: '1s',
      preAllocatedVUs: 20,
      maxVUs: 50,
      startTime: '30s',
      stages: [
        { target: 10, duration: '1m' },
      ],
    },
    ramp2: {
      executor: 'ramping-arrival-rate',
      startRate: 10,
      timeUnit: '1s',
      preAllocatedVUs: 50,
      maxVUs: 100,
      startTime: '1m30s',
      stages: [
        { target: 30, duration: '2m' },
      ],
    },
    soak: {
      executor: 'constant-arrival-rate',
      rate: 30,
      timeUnit: '1s',
      duration: '5m',
      preAllocatedVUs: 100,
      maxVUs: 150,
      startTime: '3m30s',
    },
    step: {
      executor: 'ramping-arrival-rate',
      startRate: 30,
      timeUnit: '1s',
      preAllocatedVUs: 150,
      maxVUs: 200,
      startTime: '8m30s',
      stages: [
        { target: 60, duration: '2m' },
        { target: 0, duration: '30s' }, // cooldown
      ],
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<500', 'p(99)<1000'],
  },
};

export default function () {
  const body = {
    userId: Number(USER_ID),
    supplierId: SUPPLIER_ID,
    userCouponId: null,
    comment: 'load test',
    addressId: ADDRESS_ID,
    deliveryLatitude: 37.5665,
    deliveryLongitude: 126.9780,
    orderItems: items[0],
  };

  const res = http.post(`${BASE_URL}/v1/orders`, JSON.stringify(body), {
    headers: { 'Content-Type': 'application/json' },
  });

  check(res, {
    'status 201/200': (r) => r.status === 201 || r.status === 200,
  });

  sleep(0.1);
}
