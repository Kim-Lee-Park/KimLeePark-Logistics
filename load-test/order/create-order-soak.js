import { postOrder, getInventoryQty, teardownCheck } from './common.js';

export const options = {
  scenarios: {
    soak: {
      executor: 'constant-arrival-rate',
      rate: 10,
      timeUnit: '1s',
      duration: '3m',
      preAllocatedVUs: 30,
      maxVUs: 60,
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<800'],
  },
};

export function setup() {
  return { initialQty: getInventoryQty() };
}

export default function () {
  postOrder();
}

export function teardown(data) {
  teardownCheck(data.initialQty);
}
