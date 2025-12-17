import {postOrder} from './common.js';

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
    http_req_duration: ['p(95)<800'],
  },
};

export default function () {
  postOrder();
}
