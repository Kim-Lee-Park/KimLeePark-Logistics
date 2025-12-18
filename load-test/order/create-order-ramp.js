import {postOrder} from './common.js';

export const options = {
  scenarios: {
    ramp: {
      executor: 'ramping-arrival-rate',
      startRate: 2,
      timeUnit: '1s',
      preAllocatedVUs: 20,
      maxVUs: 50,
      stages: [
        {target: 10, duration: '1m'},
        {target: 30, duration: '2m'},
        {target: 0, duration: '30s'},
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
