import { postOrder } from './common.js';

export const options = {
  scenarios: {
    step: {
      executor: 'ramping-arrival-rate',
      startRate: 10,
      timeUnit: '1s',
      preAllocatedVUs: 50,
      maxVUs: 120,
      stages: [
        { target: 30, duration: '2m' },
        { target: 0, duration: '30s' }, // cooldown
      ],
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<800'],
  },
};

export default function () {
  postOrder();
}
