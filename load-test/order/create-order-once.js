import { postOrder } from './common.js';

export const options = {
  scenarios: {
    once: {
      executor: 'per-vu-iterations',
      vus: 1,
      iterations: 1,
    },
  },
  thresholds: {
    http_req_failed: ['rate==0'],
    http_req_duration: ['p(95)<800'],
  },
};

export default function () {
  postOrder();
}
