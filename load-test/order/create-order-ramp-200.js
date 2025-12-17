import {postOrder} from './common.js';

export const options = {
  scenarios: {
    ramp_200: {
      executor: 'ramping-arrival-rate',
      startRate: 20,
      timeUnit: '1s',
      preAllocatedVUs: 250,
      maxVUs: 350,
      stages: [
        {target: 80, duration: '1m'},
        {target: 150, duration: '1m'},
        {target: 200, duration: '2m'},
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
